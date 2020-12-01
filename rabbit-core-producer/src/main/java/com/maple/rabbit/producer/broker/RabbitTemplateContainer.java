package com.maple.rabbit.producer.broker;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import com.maple.rabbit.api.Message;
import com.maple.rabbit.api.MessageType;
import com.maple.rabbit.api.exception.MessageRunTimeException;
import com.maple.rabbit.common.convert.GenericMessageConverter;
import com.maple.rabbit.common.convert.RabbitMessageConverter;
import com.maple.rabbit.common.serializer.Serializer;
import com.maple.rabbit.common.serializer.SerializerFactory;
import com.maple.rabbit.common.serializer.impl.JacksonSerializerFactory;
import com.maple.rabbit.producer.service.MessageStoreService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 类描述：RabbitTemplate池化封装
 *
 * <p>
 * 每一个topic 对应一个RabbitTemplate
 * 1. 提高发送的效率
 * 2. 可以根据不同的需求定制化不同的RabbitTemplate
 * </p>
 *
 * @author hzc
 * @date 2020/11/22 1:52 下午
 */
@Slf4j
@Component
public class RabbitTemplateContainer implements RabbitTemplate.ConfirmCallback {

    private Map<String, RabbitTemplate> rabbitMap = Maps.newConcurrentMap();

    private Splitter splitter = Splitter.on("#");

    private SerializerFactory serializerFactory = JacksonSerializerFactory.INSTANCE;

    @Autowired
    private ConnectionFactory connectionFactory;

    @Autowired
    private MessageStoreService messageStoreService;

    public RabbitTemplate getTemplate(Message message) throws MessageRunTimeException {
        Preconditions.checkNotNull(message);
        String topic = message.getTopic();
        RabbitTemplate rabbitTemplate = rabbitMap.get(topic);

        if (rabbitTemplate != null) {
            return rabbitTemplate;
        }

        log.info("#RabbitTemplateContainer.getTemplate# topic: {} is not exits.", topic);

        RabbitTemplate newTemplate = new RabbitTemplate(connectionFactory);
        newTemplate.setExchange(topic);
        newTemplate.setRoutingKey(message.getRoutingKey());
        newTemplate.setRetryTemplate(new RetryTemplate());

        // 添加序列化反序列化和converter对象
        Serializer serializer = serializerFactory.create();
        GenericMessageConverter gmc = new GenericMessageConverter(serializer);
        RabbitMessageConverter rmc = new RabbitMessageConverter(gmc);
        newTemplate.setMessageConverter(rmc);

        if (!MessageType.RAPID.equals(message.getMessageType())) {
            newTemplate.setConfirmCallback(this);
        }
        rabbitMap.putIfAbsent(topic, newTemplate);
        return rabbitMap.get(topic);
    }

    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String s) {
        List<String> strings = splitter.splitToList(correlationData.getId());

        String messageId = strings.get(0);
        long sendTime = Long.parseLong(strings.get(1));

        if (ack) {
            // 当broker返回ACK成功时，更新日志表里对应的消息状态为SEND_OK
            messageStoreService.success(messageId);
            log.info("send message is OK, confirm messageId: {}, sendTime: {}", messageId, sendTime);
        } else {
            log.error("send message is Fail, confirm messageId: {}, sendTime: {}", messageId, sendTime);
        }
    }
}
