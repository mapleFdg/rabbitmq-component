package com.maple.rabbit.api;

import com.maple.rabbit.api.exception.MessageRunTimeException;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 类描述：构建Message，建造者模式
 *
 * @author hzc
 * @date 2020/11/22 11:13 上午
 */
public class MessageBuilder {

    private String messageId;

    private String topic;

    private String routingKey = "";

    private Map<String, Object> attributes = new HashMap<String, Object>();

    private int delayMills;

    private String messageType = MessageType.CONFIRM;

    private MessageBuilder() {
    }

    public static MessageBuilder create() {
        return new MessageBuilder();
    }

    public MessageBuilder withMessageId(String messageId) {
        this.messageId = messageId;
        return this;
    }

    public MessageBuilder withTopic(String topic) {
        this.topic = topic;
        return this;
    }

    public MessageBuilder withRoutingKey(String routingKey) {
        this.routingKey = routingKey;
        return this;
    }

    public MessageBuilder withAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
        return this;
    }

    public MessageBuilder withAttribute(String key, Object value) {
        this.attributes.put(key, value);
        return this;
    }

    public MessageBuilder withDelayMills(int delayMills) {
        this.delayMills = delayMills;
        return this;
    }

    public MessageBuilder withMessageType(String messageType) {
        this.messageType = messageType;
        return this;
    }

    public Message build() {
        // 1. check messageId
        if(messageId == null) {
            messageId = UUID.randomUUID().toString();
        }
        // 2. topic is null
        if(topic == null) {
            throw new MessageRunTimeException("this topic is null");
        }
        return new Message(messageId, topic, routingKey, attributes, delayMills, messageType);
    }
}
