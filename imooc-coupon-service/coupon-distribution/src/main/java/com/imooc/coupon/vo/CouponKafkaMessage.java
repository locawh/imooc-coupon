package com.imooc.coupon.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author wangheng
 * description 优惠券 kafka 消息对象定义
 * date 2020-02-09 01:23
 * version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CouponKafkaMessage {

    /**
     * 优惠券状态
     */
    private Integer status;

    /**
     * Coupon主键
     */
    private List<Integer> ids;
}
