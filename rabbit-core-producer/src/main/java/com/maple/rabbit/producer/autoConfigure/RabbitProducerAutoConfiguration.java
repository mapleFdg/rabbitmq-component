package com.maple.rabbit.producer.autoConfigure;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * 类描述：自动装配
 *
 * @author hzc
 * @date 2020/11/22 12:22 下午
 */
@Configuration
@ComponentScan(basePackages={"com.maple.rabbit.producer"})
public class RabbitProducerAutoConfiguration {
}
