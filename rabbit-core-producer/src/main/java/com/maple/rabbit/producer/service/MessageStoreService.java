package com.maple.rabbit.producer.service;

import com.maple.rabbit.producer.entity.BrokerMessage;

/**
 * 接口描述：消息业务类
 *
 * @author hzc
 * @date 2020/11/23 8:50 下午
 */
public interface MessageStoreService {

    /**
     * 插入消息
     *
     * @param brokerMessage
     * @return
     */
    int insert(BrokerMessage brokerMessage);

    /**
     * 消息ACK成功
     *
     * @param messageId
     */
    void success(String messageId);

    /**
     * 消息ACK失败
     *
     * @param messageId
     */
    void failure(String messageId);


}
