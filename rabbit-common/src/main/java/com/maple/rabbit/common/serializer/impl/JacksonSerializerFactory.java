package com.maple.rabbit.common.serializer.impl;

import com.maple.rabbit.common.serializer.Serializer;
import com.maple.rabbit.common.serializer.SerializerFactory;
import com.maple.rabbit.api.Message;

/**
 * 类描述：Jackson序列化工厂
 *
 * @author hzc
 * @date 2020/11/22 5:30 下午
 */
public class JacksonSerializerFactory implements SerializerFactory {

    public static final SerializerFactory INSTANCE = new JacksonSerializerFactory();

    @Override
    public Serializer create() {
        return JacksonSerializer.createParametricType(Message.class);
    }
}
