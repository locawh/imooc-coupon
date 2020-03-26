package com.imooc.coupon.filter;

import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;

/**
 * Created by Qinyi.
 */
public abstract class AbstractPreZuulFilter extends AbstractZuulFilter {


    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE;
    }
}
