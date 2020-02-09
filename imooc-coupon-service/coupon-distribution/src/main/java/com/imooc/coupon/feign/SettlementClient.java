package com.imooc.coupon.feign;

import com.imooc.coupon.exception.CouponException;
import com.imooc.coupon.feign.hystrix.SettlementClientHystrix;
import com.imooc.coupon.vo.CommonResponse;
import com.imooc.coupon.vo.SettlementInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author wangheng
 * description 优惠券结算微服务 Feign 接口定义
 * date 2020-02-09 02:20
 * version 1.0
 */
@SuppressWarnings("all")
@FeignClient(value = "eureka-client-coupon-settlement",fallback = SettlementClientHystrix.class)
public interface SettlementClient {

    /**
     * <h2>优惠券规则计算</h2>
     */
    @RequestMapping(value = "/coupon-settlement/settlement/compute",
            method = RequestMethod.POST)
    CommonResponse<SettlementInfo> computeRule(
            @RequestBody SettlementInfo settlement) throws CouponException;

}
