package com.maple.rabbit.demo;

import com.maple.rabbit.api.Message;
import com.maple.rabbit.api.MessageBuilder;
import com.maple.rabbit.api.MessageType;
import com.maple.rabbit.producer.broker.ProducerClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 类描述：
 *
 * @author hzc
 * @date 2021/4/20 11:55 上午
 */
@RestController
public class TestController {

    @Autowired
    private ProducerClient producerClient;

    @GetMapping("/test")
    public String test(){
        Message message = MessageBuilder.create().withMessageType(MessageType.RELIANT)
                .withTopic("exchange").withRoutingKey("test").withAttribute("test",123).build();
        producerClient.send(message);
        return "success";
    }


}
