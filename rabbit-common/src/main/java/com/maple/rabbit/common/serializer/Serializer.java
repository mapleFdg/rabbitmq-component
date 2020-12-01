package com.maple.rabbit.common.serializer;

/**
 * 接口描述：序列化与反序列化
 *
 * @author hzc
 * @date 2020/11/22 5:17 下午
 */
public interface Serializer {

    byte[] serializeRaw(Object data);

    String serializer(Object data);

    <T> T deserialize(String content);

    <T> T deserialize(byte[] content);

}
