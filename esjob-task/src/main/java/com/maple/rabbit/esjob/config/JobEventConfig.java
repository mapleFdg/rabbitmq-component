package com.maple.rabbit.esjob.config;

import com.dangdang.ddframe.job.event.JobEventConfiguration;
import com.dangdang.ddframe.job.event.rdb.JobEventRdbConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * 类描述： 声明 esjob 连接数据库的事件，用于记录日志轨迹
 *
 * @author hzc
 * @date 2020/11/25 11:48 下午
 */
//@Configuration
public class JobEventConfig {

    @Autowired
    private DataSource dataSource;

    @Bean
    public JobEventConfiguration jobEventConfiguration(){
        return new JobEventRdbConfiguration(dataSource);
    }

}
