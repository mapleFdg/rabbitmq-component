package com.maple.rabbit.common.convert;

import com.google.common.base.Preconditions;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.amqp.support.converter.MessageConverter;

/**
 * 类描述：通过装饰者模式，自定义Message的属性
 *
 * @author hzc
 * @date 2020/11/22 5:42 下午
 */
public class RabbitMessageConverter implements MessageConverter {

    private GenericMessageConverter delegate;

    public RabbitMessageConverter(GenericMessageConverter genericMessageConverter) {
        Preconditions.checkNotNull(genericMessageConverter);
        this.delegate = genericMessageConverter;
    }

    @Override
    public Message toMessage(Object o, MessageProperties messageProperties) throws MessageConversionException {
        com.maple.rabbit.api.Message message = (com.maple.rabbit.api.Message)o;
        messageProperties.setDelay(message.getDelayMills());
        return this.delegate.toMessage(o, messageProperties);
    }

    @Override
    public Object fromMessage(Message message) throws MessageConversionException {
        com.maple.rabbit.api.Message msg = (com.maple.rabbit.api.Message) this.delegate.fromMessage(message);
        return msg;
    }
}
