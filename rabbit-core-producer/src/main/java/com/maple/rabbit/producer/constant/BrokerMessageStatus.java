package com.maple.rabbit.producer.constant;

/**
 * 类描述：消息状态枚举类
 *
 * @author hzc
 * @date 2020/11/23 8:53 下午
 */
public enum BrokerMessageStatus {

    /**
     * 字典值
     */
    SENDING("0"),
    SEND_OK("1"),
    SEND_FAIL("2");

    private String code;

    private BrokerMessageStatus(String code){
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
