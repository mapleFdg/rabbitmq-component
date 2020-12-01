package com.maple.rabbit.common.serializer;

/**
 * 接口描述：创建序列化类
 *
 * @author hzc
 * @date 2020/11/22 5:20 下午
 */
public interface SerializerFactory {

    Serializer create();
}
