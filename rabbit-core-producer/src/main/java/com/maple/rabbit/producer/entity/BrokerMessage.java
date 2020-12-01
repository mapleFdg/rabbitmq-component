package com.maple.rabbit.producer.entity;

import com.maple.rabbit.api.Message;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 类描述：数据库 表 broker_message 实体类
 *
 * @author hzc
 * @date 2020/11/22 11:03 下午
 */
@Data
public class BrokerMessage implements Serializable {

    private static final long serialVersionUID = 8361664602568985099L;

    private String messageId;

    private Message message;

    private Integer tryCount = 0;

    private String status;

    private Date nextRetry;

    private Date createTime;

    private Date updateTime;

    public void setMessageId(String messageId) {
        this.messageId = messageId == null ? null : messageId.trim();
    }

    public void setStatus(String status) {
        this.status = status == null ? null : status.trim();
    }

}
