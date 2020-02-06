package com.imooc.coupon.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * @author wangheng
 * description 产品线枚举
 * date 2020-02-05 17:38
 * version 1.0
 */
@Getter
@AllArgsConstructor
public enum ProductLine {
    /**
     * 大猫
     */
    DAOMA("大猫", 1),
    /**
     * 大宝
     */
    DAOBA("大宝", 2);

    /**
     * 产品线描述 分类
     */
    private String description;

    /**
     * 产品线分类 编码
     */
    private Integer code;

    public static ProductLine of(Integer code) {
        Objects.requireNonNull(code);
        return Stream.of(values())
                .filter(bean -> bean.code.equals(code))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException(code + "not exist!"));
    }
}
