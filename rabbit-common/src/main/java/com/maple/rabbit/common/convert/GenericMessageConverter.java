package com.maple.rabbit.common.convert;

import com.google.common.base.Preconditions;
import com.maple.rabbit.common.serializer.Serializer;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.amqp.support.converter.MessageConverter;

/**
 * 类描述：自定义的消息类型与amqp的消息类型通过序列化相互转换
 *
 * @author hzc
 * @date 2020/11/22 5:35 下午
 */
public class GenericMessageConverter implements MessageConverter {

    /**
     * 序列化方式
     */
    private Serializer serializer;

    public GenericMessageConverter(Serializer serializer){
        Preconditions.checkNotNull(serializer);
        this.serializer = serializer;
    }

    @Override
    public Object fromMessage(Message message) throws MessageConversionException {
        return this.serializer.deserialize(message.getBody());
    }

    @Override
    public Message toMessage(Object o, MessageProperties messageProperties) throws MessageConversionException {
        return new Message(this.serializer.serializeRaw(o),messageProperties);
    }


}
