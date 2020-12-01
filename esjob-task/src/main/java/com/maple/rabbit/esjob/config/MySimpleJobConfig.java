package com.maple.rabbit.esjob.config;

import com.dangdang.ddframe.job.api.simple.SimpleJob;
import com.dangdang.ddframe.job.config.JobCoreConfiguration;
import com.dangdang.ddframe.job.config.simple.SimpleJobConfiguration;
import com.dangdang.ddframe.job.event.JobEventConfiguration;
import com.dangdang.ddframe.job.lite.api.JobScheduler;
import com.dangdang.ddframe.job.lite.config.LiteJobConfiguration;
import com.dangdang.ddframe.job.lite.spring.api.SpringJobScheduler;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;
import com.maple.rabbit.esjob.listener.SimpleJobListener;
import com.maple.rabbit.esjob.task.MySimpleJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import javax.swing.*;

/**
 * 类描述：声明SimpleJob(包括监听，配置等)
 *
 * @author hzc
 * @date 2020/11/25 11:33 下午
 */
//@Configuration
public class MySimpleJobConfig {

    @Resource(name = "registryCenter")
    private ZookeeperRegistryCenter registryCenter;

    @Autowired
    private JobEventConfiguration jobEventConfiguration;

    @Value("${simpleJob.cron}")
    private String cron;
    @Value("${simpleJob.shardingTotalCount}")
    private int shardingTotalCount;
    @Value("${simpleJob.shardingItemParameters}")
    private String shardingItemParameters;
    @Value("${simpleJob.jobParameter}")
    private String jobParameter;
    @Value("${simpleJob.failover}")
    private boolean failover;
    @Value("${simpleJob.monitorExecution}")
    private boolean monitorExecution;
    @Value("${simpleJob.monitorPort}")
    private int monitorPort;
    @Value("${simpleJob.maxTimeDiffSeconds}")
    private int maxTimeDiffSeconds;
    @Value("${simpleJob.jobShardingStrategyClass}")
    private String jobShardingStrategyClass;


    @Bean
    public SimpleJob simpleJob() {
        return new MySimpleJob();
    }

    @Bean(initMethod = "init")
    public JobScheduler simpleJobSchedule(SimpleJob simpleJob) {
        return new SpringJobScheduler(simpleJob, registryCenter, getLiteJobConfiguration(simpleJob.getClass()),
                jobEventConfiguration, new SimpleJobListener());
    }

    private LiteJobConfiguration getLiteJobConfiguration(Class<? extends SimpleJob> jobClass) {
        JobCoreConfiguration jobCoreConfiguration = JobCoreConfiguration
                .newBuilder(jobClass.getName(), cron, shardingTotalCount)
                .misfire(true)
                .failover(failover)
                .jobParameter(jobParameter)
                .shardingItemParameters(shardingItemParameters)
                .build();

        SimpleJobConfiguration simpleJobConfiguration = new SimpleJobConfiguration(jobCoreConfiguration, jobClass.getCanonicalName());

        LiteJobConfiguration liteJobConfiguration = LiteJobConfiguration.newBuilder(simpleJobConfiguration)
                .jobShardingStrategyClass(jobShardingStrategyClass)
                .monitorExecution(monitorExecution)
                .monitorPort(monitorPort)
                .maxTimeDiffSeconds(maxTimeDiffSeconds)
                .overwrite(true)
                .build();
        return liteJobConfiguration;
    }

}
