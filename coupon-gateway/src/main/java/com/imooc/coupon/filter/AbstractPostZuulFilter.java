package com.imooc.coupon.filter;

import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;

/**
 * Created by Qinyi.
 */
public abstract class AbstractPostZuulFilter extends AbstractZuulFilter {


    @Override
    public String filterType() {
        return FilterConstants.POST_TYPE;
    }
}
