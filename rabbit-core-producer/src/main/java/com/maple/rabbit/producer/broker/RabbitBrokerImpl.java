package com.maple.rabbit.producer.broker;

import com.maple.rabbit.api.Message;
import com.maple.rabbit.api.MessageType;
import com.maple.rabbit.producer.constant.BrokerMessageConst;
import com.maple.rabbit.producer.constant.BrokerMessageStatus;
import com.maple.rabbit.producer.entity.BrokerMessage;
import com.maple.rabbit.producer.service.MessageStoreService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 类描述：真正的发送不同类型消息的实现类
 *
 * @author hzc
 * @date 2020/11/22 1:13 下午
 */
@Slf4j
@Component
public class RabbitBrokerImpl implements RabbitBroker {

    @Autowired
    private RabbitTemplateContainer rabbitTemplateContainer;

    @Autowired
    private MessageStoreService messageStoreService;

    @Override
    public void rapidSend(Message message) {
        message.setMessageType(MessageType.RAPID);
        sendKernel(message);
    }

    /**
     * 方法描述： 发送消息的核心方法，使用异步线程池进行消息发送
     *
     * @param message
     */
    private void sendKernel(Message message) {
        AsyncBaseQueue.submit(() -> {
            CorrelationData correlationData = new CorrelationData(String.format("%s#%s", message.getMessageId(), System.currentTimeMillis()));
            String topic = message.getTopic();
            String routingKey = message.getRoutingKey();
            RabbitTemplate rabbitTemplate = rabbitTemplateContainer.getTemplate(message);
            rabbitTemplate.convertAndSend(topic, routingKey, message, correlationData);
            log.info("#RabbitBrokerImpl.sendKernel# send to rabbitmq, messageId : {}", message.getMessageId());
        });

    }

    @Override
    public void confirmSend(Message message) {
        message.setMessageType(MessageType.CONFIRM);
        sendKernel(message);
    }

    @Override
    public void reliantSend(Message message) {
        message.setMessageType(MessageType.RELIANT);

        /**
         * 1.消息入库
         */
        Date now = new Date();
        BrokerMessage brokerMessage = new BrokerMessage();
        brokerMessage.setMessageId(message.getMessageId());
        brokerMessage.setCreateTime(now);
        brokerMessage.setUpdateTime(now);
        brokerMessage.setStatus(BrokerMessageStatus.SENDING.getCode());
        // 设置下次重试时间
        brokerMessage.setNextRetry(DateUtils.addMinutes(now, BrokerMessageConst.TIMEOUT));
        brokerMessage.setMessage(message);
        messageStoreService.insert(brokerMessage);

        /**
         * 2. 真正发送消息
         */
        sendKernel(message);
    }

    @Override
    public void sendMessages() {

    }
}

