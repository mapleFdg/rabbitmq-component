package com.maple.rabbit.producer.broker;

import com.maple.rabbit.api.Message;

/**
 * 接口描述：具体发送不同种类型消息的接口
 *
 * @author hzc
 * @date 2020/11/22 1:11 下午
 */
public interface RabbitBroker {

    void rapidSend(Message message);

    void confirmSend(Message message);

    void reliantSend(Message message);

    void sendMessages();

}
