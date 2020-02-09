package com.imooc.coupon.feign.hystrix;

import com.imooc.coupon.feign.TemplateClient;
import com.imooc.coupon.vo.CommonResponse;
import com.imooc.coupon.vo.CouponTemplateSDK;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author wangheng
 * description 优惠券模板 Feign 接口的熔断降级策略
 * date 2020-02-09 02:27
 * version 1.0
 */
@Slf4j
@Component
public class TemplateClientHystrix implements TemplateClient {

    /**
     * <h2>查找所有可用的优惠券模板</h2>
     */
    @Override
    public CommonResponse<List<CouponTemplateSDK>> findAllUsableTemplate() {

        log.error("[eureka-client-coupon-template] findAllUsableTemplate " +
                "request error");

        return new CommonResponse<>(
                -1,
                "[eureka-client-coupon-template] request error",
                Collections.emptyList()
        );
    }

    /**
     * <h2>获取模板 ids 到 CouponTemplateSDK 的映射</h2>
     *
     * @param ids 优惠券模板 id
     */
    @Override
    public CommonResponse<Map<Integer, CouponTemplateSDK>> findIds2TemplateSDK(Collection<Integer> ids) {

        log.error("[eureka-client-coupon-template] findIds2TemplateSDK" +
                "request error");

        return new CommonResponse<>(
                -1,
                "[eureka-client-coupon-template] request error",
                new HashMap<>()
        );
    }

}
