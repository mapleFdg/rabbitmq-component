package com.maple.rabbit.producer.broker;

import com.google.common.base.Preconditions;
import com.maple.rabbit.api.Message;
import com.maple.rabbit.api.MessageProducer;
import com.maple.rabbit.api.MessageType;
import com.maple.rabbit.api.SendCallback;
import com.maple.rabbit.api.exception.MessageRunTimeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 类描述：发送消息的实际实现类
 *
 * @author hzc
 * @date 2020/11/22 12:39 下午
 */
@Component
public class ProducerClient implements MessageProducer {

    @Autowired
    private RabbitBroker rabbitBroker;

    @Override
    public void send(Message message) throws MessageRunTimeException {
        // 检查topic是否为空
        Preconditions.checkNotNull(message.getTopic());
        String messageType = message.getMessageType();
        switch (messageType) {
            case MessageType.RAPID:
                rabbitBroker.rapidSend(message);
                break;
            case MessageType.CONFIRM:
                rabbitBroker.confirmSend(message);
                break;
            case MessageType.RELIANT:
                rabbitBroker.reliantSend(message);
                break;
            default:
                break;
        }
    }

    @Override
    public void send(List<Message> messages) throws MessageRunTimeException {

    }

    @Override
    public void send(Message message, SendCallback sendCallback) throws MessageRunTimeException {

    }

}
