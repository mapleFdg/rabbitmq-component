package com.maple.rabbit.producer.service.impl;

import com.maple.rabbit.producer.constant.BrokerMessageStatus;
import com.maple.rabbit.producer.entity.BrokerMessage;
import com.maple.rabbit.producer.mapper.BrokerMessageMapper;
import com.maple.rabbit.producer.service.MessageStoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 类描述：消息业务实现类
 *
 * @author hzc
 * @date 2020/11/23 8:51 下午
 */
@Service
public class MessageStoreServiceImpl implements MessageStoreService {

    @Autowired
    private BrokerMessageMapper brokerMessageMapper;

    @Override
    public int insert(BrokerMessage brokerMessage) {
        return brokerMessageMapper.insert(brokerMessage);
    }

    @Override
    public void success(String messageId) {
        brokerMessageMapper.changeBrokerMessageStatus(messageId, BrokerMessageStatus.SEND_OK.getCode(),new Date());
    }

    @Override
    public void failure(String messageId) {
        brokerMessageMapper.changeBrokerMessageStatus(messageId, BrokerMessageStatus.SEND_FAIL.getCode(),new Date());
    }
}
