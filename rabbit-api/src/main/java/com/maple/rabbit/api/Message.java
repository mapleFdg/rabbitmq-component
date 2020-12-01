package com.maple.rabbit.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 消息实体类
 * @author hzc
 * @date 2020/11/22 10:54 上午
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message implements Serializable {

    private static final long serialVersionUID = -1083270368180788044L;

    /**
     * 消息的唯一ID
     */
    private String messageId;

    /**
     * 消息的主题
     */
    private String topic;

    /**
     * 消息的路由规则
     */
    private String routingKey = "";

    /**
     * 消息的附加属性
     */
    private Map<String, Object> attributes = new HashMap<String, Object>();

    /**
     * 延迟消息的参数配置
     */
    private int delayMills;

    /**
     * 消息类型
     */
    private String messageType = MessageType.CONFIRM;

}
