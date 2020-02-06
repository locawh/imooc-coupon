package com.imooc.coupon.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * @author wangheng
 * description 分发目标枚举
 * date 2020-02-05 17:46
 * version 1.0
 */
@Getter
@AllArgsConstructor
public enum DistributeTarget {
    /**
     * 单用户
     */
    SINGLE("单用户", 1),
    /**
     * 多用户
     */
    MULTI("多用户", 2);

    /**
     * 分发目标描述 分类
     */
    private String description;

    /**
     * 分发目标分类 编码
     */
    private Integer code;

    public static DistributeTarget of(Integer code) {
        Objects.requireNonNull(code);
        return Stream.of(values())
                .filter(bean -> bean.code.equals(code))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException(code + "not exist!"));
    }
}
