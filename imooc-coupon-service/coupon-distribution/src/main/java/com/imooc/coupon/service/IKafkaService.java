package com.imooc.coupon.service;

import org.apache.kafka.clients.consumer.ConsumerRecord;

/**
 * @author wangheng
 * description <h1> Kafka相关操作服务接口定义</h1>
 * date 2020-02-08 02:52
 * version 1.0
 */
public interface IKafkaService {

    /**
     * <h2>消费优惠券 Kafka 消息</h2>
     *
     * @param record {@link ConsumerRecord}
     */
    void consumeCouponKafkaMessage(ConsumerRecord<?, ?> record);
}
