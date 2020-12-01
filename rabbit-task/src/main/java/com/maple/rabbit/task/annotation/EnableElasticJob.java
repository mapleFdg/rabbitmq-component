package com.maple.rabbit.task.annotation;

import com.maple.rabbit.task.config.JobParserAutoConfigurartion;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 注解描述：加入注解后，自动装配JobParserAutoConfigurartion类
 *
 * @author hzc
 * @date 2020/11/26 11:09 下午
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import(JobParserAutoConfigurartion.class)
public @interface EnableElasticJob {
}
