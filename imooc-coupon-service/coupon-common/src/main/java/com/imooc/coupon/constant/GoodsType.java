package com.imooc.coupon.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * @author wangheng
 * description 商品类型枚举
 * date 2020-02-08 03:06
 * version 1.0
 */
@Getter
@AllArgsConstructor
public enum GoodsType {

    /**
     * 文娱
     */
    WENYU("文娱", 1),
    /**
     * 生鲜
     */
    SHENGXIAN("生鲜", 2),
    /**
     * 家居
     */
    JIAJU("家居", 3),
    /**
     * 其它
     */
    OTHERS("其它", 4),
    /**
     * 全品类
     */
    ALL("全品类", 5);

    /**
     * 商品描述
     */
    private String description;
    /**
     * 商品编码
     */
    private Integer code;

    public static GoodsType of(Integer code) {
        Objects.requireNonNull(code);

        return Stream.of(values())
                .filter(bean -> bean.code.equals(code))
                .findAny()
                .orElseThrow(
                        () -> new IllegalArgumentException(code + "not exists!")
                );
    }

}
