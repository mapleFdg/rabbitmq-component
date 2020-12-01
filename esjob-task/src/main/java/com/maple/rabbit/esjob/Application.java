package com.maple.rabbit.esjob;

import com.maple.rabbit.task.annotation.EnableElasticJob;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
/**
 * 类描述：SpringBoot启动类
 *
 * @author hzc
 * @date 2020/11/25 11:30 下午
 */
@EnableElasticJob
@SpringBootApplication
//@ComponentScan(basePackages = {"com.maple.rabbit.esjob"})
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class,args);
    }

}
