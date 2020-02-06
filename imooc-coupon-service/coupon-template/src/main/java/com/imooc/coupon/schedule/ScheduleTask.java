package com.imooc.coupon.schedule;

import com.imooc.coupon.dao.CouponTemplateDao;
import com.imooc.coupon.entity.CouponTemplate;
import com.imooc.coupon.vo.TemplateRule;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author wangheng
 * description 定时清理已过期的优惠券模板
 * date 2020-02-07 00:31
 * version 1.0
 */
@Slf4j
@Component
public class ScheduleTask {

    /**
     * 注入CouponTemplate Dao
     */
    private final CouponTemplateDao templateDao;

    @Autowired
    public ScheduleTask(CouponTemplateDao templateDao) {
        this.templateDao = templateDao;
    }

    /**
     * <h2>下线已过期的优惠券模板</h2>
     */
    @Scheduled(fixedRate = 60 * 60 * 1000)
    public void offlineCouponTemplate() {
        log.info("Start TExpire CouponTemplate");

        List<CouponTemplate> templates = templateDao.findAllByExpired(false);
        if (CollectionUtils.isEmpty(templates)) {
            log.info("Done To Expire CouponTemplate.");
            return;
        }

        Date cur = new Date();
        List<CouponTemplate> expiredTemplates = new ArrayList<>(templates.size());
        templates.forEach(t -> {
            // 根据优惠券模板规则中的 "过期规则" 校验模板是否过期
            TemplateRule rule = t.getRule();
            if (rule.getExpiration().getDeadline() < cur.getTime()) {
                t.setExpired(true);
                expiredTemplates.add(t);
            }
        });

        if (CollectionUtils.isNotEmpty(expiredTemplates)) {
            log.info("Expired CouponTemplate Num: {}", templateDao.saveAll(expiredTemplates));
        }
        log.info("Done To Expire CouponTemplate.");
    }
}
