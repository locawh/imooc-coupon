package com.imooc.coupon.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author wangheng
 * description fake 商品信息定义
 * date 2020-02-08 03:20
 * version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoodsInfo {

    /**
     * 商品类型: {@link com.imooc.coupon.constant.GoodsType }
     */
    private Integer type;

    /**
     * 商品价格
     */
    private Double price;

    /**
     * 商品数量
     */
    private Integer count;

    //TODO 商品名称  商品使用信息
}
