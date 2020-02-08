package com.imooc.coupon.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author wangheng
 * description <h1>结算信息对象定义</h1>
 * 1. userId
 * 2. 商品信息列表
 * 3. 优惠券列表
 * 4. 结算结果金额
 * date 2020-02-08 03:29
 * version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SettlementInfo {

    /**
     * 用户 id
     */
    private Long userId;

    /**
     * 商品信息
     */
    private List<GoodsInfo> goodsInfos;
    /**
     * 优惠券列表
     */
    private List<CouponAndTemplateInfo> couponAndTemplateInfos;

    /**
     * 是否使结算生效,即核销  true 结算生效  false 仅完成商品金额结算
     */
    private Boolean employ;

    /**
     * 结果结算金额
     */
    private Double cost;
    /**
     * <h2>优惠券和模板信息</h2>
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CouponAndTemplateInfo {

        /**
         * coupon 的主键id
         */
        private Integer id;

        /**
         *  优惠券对应的模板对象
         */
        private CouponTemplateSDK template;
    }

}
