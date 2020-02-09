package com.imooc.coupon.service.impl;

import com.alibaba.fastjson.JSON;
import com.imooc.coupon.constant.Constant;
import com.imooc.coupon.constant.CouponStatus;
import com.imooc.coupon.dao.CouponDao;
import com.imooc.coupon.entity.Coupon;
import com.imooc.coupon.exception.CouponException;
import com.imooc.coupon.feign.SettlementClient;
import com.imooc.coupon.feign.TemplateClient;
import com.imooc.coupon.service.IRedisService;
import com.imooc.coupon.service.IUserService;
import com.imooc.coupon.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <h1>用户服务相关接口实现</h1>
 * 所有的操作过程, 状态都保存在 Redis 中, 并通过 Kafka 把消息异步传递到 MySQL中
 * (为什么使用 Kafka, 而不是直接使用 SpringBoot 中的异步处理 ?
 * 原因:  主要是保证服务的安全性, 因为异步任务是可能会失败的,但是在 kafka中,
 * 即使消息失败了,也可以重新去获取消息,回馈记录,
 * 这样就能够保证 Cache和存储库中消息的一致性,因为我们所有的操作都是基于 Redis去做的,
 * MySql只是作为底层的存储而存在的,所以确保底层存储和Cache缓存的消息一致性很重要)
 *
 * @author wangheng
 * @version 1.0
 * @date 2020-02-09 15:29
 */
@Slf4j
@Service
@SuppressWarnings("all")
public class UserServiceImpl implements IUserService {

    /**
     * Coupon Dao
     */
    private final CouponDao couponDao;

    /**
     * Redis 服务
     */
    private final IRedisService redisService;

    /**
     * 模板微服务客户端
     */
    private final TemplateClient templateClient;

    /**
     * 结算微服务客户端
     */
    private final SettlementClient settlementClient;

    /**
     * Kafka 客户端
     */
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    public UserServiceImpl(CouponDao couponDao, IRedisService redisService,
                           TemplateClient templateClient, SettlementClient settlementClient,
                           KafkaTemplate<String, String> kafkaTemplate) {
        this.couponDao = couponDao;
        this.redisService = redisService;
        this.templateClient = templateClient;
        this.settlementClient = settlementClient;
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * <h2>根据用户 id 和状态查询优惠券记录</h2>
     *
     * @param userId 用户 id
     * @param status 优惠券状态
     * @return {@link Coupon}s
     */
    @Override
    public List<Coupon> findCouponsByStatus(Long userId, Integer status) throws CouponException {

        List<Coupon> curCached = redisService.getCachedCoupons(userId, status);
        List<Coupon> preTarget;

        if (CollectionUtils.isNotEmpty(curCached)) {
            log.debug("coupon cache is not empty: {}, {}", userId, status);
            preTarget = curCached;
        } else {
            log.debug("coupon cache is empty, get coupon from db: {}, {}", userId, status);

            //缓存中没有,从数据库查询优惠券记录
            List<Coupon> dbCoupons = couponDao.findAllByUserIdAndStatus(userId, CouponStatus.of(status));

            //如果数据库中没有记录, 那么就直接返就ok, Cache 中已经加入了一张无效的优惠券
            if (CollectionUtils.isEmpty(dbCoupons)) {
                log.debug("current user do not have coupon: {}, {}", userId, status);
                return dbCoupons;
            }

            // 填充 dbCoupons的 templateSDK 字段
            Map<Integer, CouponTemplateSDK> id2TemplateSDK = templateClient.findIds2TemplateSDK(
                    dbCoupons.stream()
                            .map(Coupon::getTemplateId)
                            .collect(Collectors.toList())
            ).getData();
            dbCoupons.forEach(e ->
                    e.setTemplateSDK(
                            id2TemplateSDK.get(e.getTemplateId())
                    )
            );
            //数据库中存在记录
            preTarget = dbCoupons;
            //将记录写入 Cache 中
            redisService.addCouponToCache(userId, preTarget, status);
        }

        //将无效的优惠券剔除
        preTarget = preTarget.stream().filter(c -> c.getId() != -1).collect(Collectors.toList());
        // 如果当前获取的是可用优惠券, 还需要做对已过期优惠券的延迟处理
        if (CouponStatus.of(status) == CouponStatus.USABLE) {
            CouponClassify classify = CouponClassify.classify(preTarget);
            //如果过期状态不为空,就需要做延迟处理
            if (CollectionUtils.isNotEmpty(classify.getExpired())) {
                log.info("Add Expired Coupons To Cache From FindCouponsByStatus: " +
                        "{}, {}", userId, status);
                redisService.addCouponToCache(userId, classify.getExpired(), CouponStatus.EXPIRED.getCode());

                //发送到 kafka中做异步处理
                kafkaTemplate.send(
                        Constant.TOPIC,
                        JSON.toJSONString(new CouponKafkaMessage(CouponStatus.EXPIRED.getCode(),
                                classify.getExpired()
                                        .stream()
                                        .map(Coupon::getId)
                                        .collect(Collectors.toList())))
                );
            }
            return classify.getUsable();
        }

        return preTarget;
    }

    /**
     * <h2>根据用户 id 查找当前可以领取的优惠券模板</h2>
     *
     * @param userId 用户 id
     * @return {@link CouponTemplateSDK}s
     */
    @Override
    public List<CouponTemplateSDK> findAvailableTemplate(Long userId) throws CouponException {

        long curTime = new Date().getTime();
        List<CouponTemplateSDK> templateSDKS = templateClient.findAllUsableTemplate().getData();

        log.debug("Find All Template(From TemplateClient) Count: {}", templateSDKS.size());

        //过滤过期的优惠券模板
        templateSDKS.stream().filter(
                t -> t.getRule().getExpiration().getDeadline() > curTime
        ).collect(Collectors.toList());

        log.info("Find Usable Template Count: {}", templateSDKS.size());

        // key 是 TemplateId     value 中的 left 是 Template limitation, right 是优惠券模板
        HashMap<Integer, Pair<Integer, CouponTemplateSDK>> limit2Template = new HashMap<>(templateSDKS.size());

        templateSDKS.forEach(
                e -> limit2Template.put(e.getId(), Pair.of(e.getRule().getLimitation(), e))
        );

        ArrayList<CouponTemplateSDK> result = new ArrayList<>(limit2Template.size());
        List<Coupon> userUsableCoupons = findCouponsByStatus(userId, CouponStatus.USABLE.getCode());

        log.debug("Current User Has Usable Coupons: {}, {}", userId, userUsableCoupons.size());

        Map<Integer, List<Coupon>> templateId2Coupons = userUsableCoupons.stream()
                .collect(Collectors.groupingBy(Coupon::getTemplateId));

        // 根据 Template 的 Rule 判断是否可以领取优惠券模板
        limit2Template.forEach((k, v) -> {

            int limitation = v.getLeft();
            CouponTemplateSDK templateSDK = v.getRight();

            if (templateId2Coupons.containsKey(k) && templateId2Coupons.get(k).size() >= limitation) {
                return;
            }
            result.add(templateSDK);
        });

        return result;
    }

    /**
     * <h2>用户领取优惠券</h2>
     * 1. 从 TemplateClient 拿到对应的优惠券, 并检查是否过期
     * 2. 根据 limitation 判断用户是否可以领取
     * 3. save to db
     * 4. 填充 CouponTemplateSDK
     * 5. save to cache
     *
     * @param request {@link AcquireTemplateRequest}
     * @return {@link Coupon}
     */
    @Override
    public Coupon acquireTemplate(AcquireTemplateRequest request) throws CouponException {

        Map<Integer, CouponTemplateSDK> id2Template = templateClient.findIds2TemplateSDK(
                Collections.singletonList(request.getTemplateSDK().getId())
        ).getData();

        //优惠券模板是需要存在的
        if (id2Template.size() <= 0) {
            log.error("Can Not Acquire Template From TemplateClient: {}",
                    request.getTemplateSDK().getId());
            throw new CouponException("Can Not Acquire Template From TemplateClient");
        }

        // 用户是否可以领取这张优惠券
        List<Coupon> userUsableCoupons = findCouponsByStatus(request.getUserId(), CouponStatus.USABLE.getCode());
        Map<Integer, List<Coupon>> template2Coupons = userUsableCoupons.stream().collect(Collectors.groupingBy(Coupon::getTemplateId));

        if (template2Coupons.containsKey(request.getTemplateSDK().getId()) &&
                template2Coupons.get(request.getTemplateSDK().getId()).size() >=
                        request.getTemplateSDK().getRule().getLimitation()) {
            log.error("Exceed Template Assign Limitation: {}",
                    request.getTemplateSDK().getId());
            throw new CouponException("Exceed Template Assign Limitation");
        }

        //尝试去获取优惠券码
        String couponCode = redisService.tryToAcquireCouponCodeFromCache(request.getTemplateSDK().getId());

        if (StringUtils.isEmpty(couponCode)) {
            log.error("Can Not Acquire Coupon Code: {}",
                    request.getTemplateSDK().getId());
            throw new CouponException("Can Not Acquire Coupon Code");
        }

        Coupon newCoupon = new Coupon(
                request.getTemplateSDK().getId(), request.getUserId(), couponCode, CouponStatus.USABLE
        );

        // 填充 Coupon 对象的 CouponTemplateSDK, 一定要在放入缓存之前去填充
        newCoupon.setTemplateSDK(request.getTemplateSDK());

        //放入缓存中
        redisService.addCouponToCache(request.getUserId(), Collections.singletonList(newCoupon), CouponStatus.USABLE.getCode());

        return newCoupon;
    }

    /**
     * <h2>结算(核销)优惠券</h2>
     *
     * @param info {@link SettlementInfo}
     * @return {@link SettlementInfo}
     */
    @Override
    public SettlementInfo settlement(SettlementInfo info) throws CouponException {

        //TODO  优惠券结算代码

        return null;
    }
}
