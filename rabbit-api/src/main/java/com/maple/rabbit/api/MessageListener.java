package com.maple.rabbit.api;

/**
 * 接口描述：消费者监听消息
 *
 * @author hzc
 * @date 2020/11/22 11:20 上午
 */
public interface MessageListener {

    void onMessage(Message message);
}
