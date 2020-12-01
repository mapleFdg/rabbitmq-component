package com.maple.rabbit.api;

import com.maple.rabbit.api.exception.MessageRunTimeException;

import java.util.List;

/**
 * 接口描述：生产者发送消息
 *
 * @author hzc
 * @date 2020/11/22 11:18 上午
 */
public interface MessageProducer {

    /**
     * $send消息的发送 附带SendCallback回调执行响应的业务逻辑处理
     *
     * @param message
     * @param sendCallback
     * @throws MessageRunTimeException
     */
    void send(Message message, SendCallback sendCallback) throws MessageRunTimeException;

    /**
     * 消息的发送
     *
     * @param message
     * @throws MessageRunTimeException
     */
    void send(Message message) throws MessageRunTimeException;

    /**
     * $send 消息的批量发送
     *
     * @param messages
     * @throws MessageRunTimeException
     */
    void send(List<Message> messages) throws MessageRunTimeException;
}
