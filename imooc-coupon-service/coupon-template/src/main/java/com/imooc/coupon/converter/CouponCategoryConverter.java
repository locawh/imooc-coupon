package com.imooc.coupon.converter;

import com.imooc.coupon.constant.CouponCategory;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * @author wangheng
 * description 优惠券分类枚举属性转换器
 * date 2020-02-05 18:44
 * version 1.0
 */
@Converter
public class CouponCategoryConverter implements AttributeConverter<CouponCategory, String> {

    @Override
    public String convertToDatabaseColumn(CouponCategory couponCategory) {
        return couponCategory.getCode();
    }

    @Override
    public CouponCategory convertToEntityAttribute(String code) {
        return CouponCategory.of(code);
    }
}
