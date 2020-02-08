package com.imooc.coupon.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * @author wangheng
 * description  优惠券状态枚举
 * date 2020-02-08 02:00
 * version 1.0
 */
@Getter
@AllArgsConstructor
public enum CouponStatus {

    /**
     * 可用的
     */
    USABLE("可用的", 1),
    /**
     * 已使用的
     */
    USED("已使用的", 2),
    /**
     * 过期的(未被使用的)
     */
    EXPIRED("过期的(未被使用的)", 3);

    /**
     * 优惠券状态描述信息
     */
    private String description;

    /**
     * 优惠券状态编码
     */
    private Integer code;

    /**
     * 根据code获取到 CouponStatus
     */
    public static CouponStatus of(Integer code) {
        Objects.requireNonNull(code);

        return Stream.of(values())
                .filter(bean -> bean.code.equals(code))
                .findAny()
                .orElseThrow(
                        () -> new IllegalArgumentException(code + "not exists!")
                );
    }
}
