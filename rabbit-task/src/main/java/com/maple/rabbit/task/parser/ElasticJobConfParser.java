package com.maple.rabbit.task.parser;

import com.dangdang.ddframe.job.api.dataflow.DataflowJob;
import com.dangdang.ddframe.job.api.script.ScriptJob;
import com.dangdang.ddframe.job.api.simple.SimpleJob;
import com.dangdang.ddframe.job.config.JobCoreConfiguration;
import com.dangdang.ddframe.job.config.JobTypeConfiguration;
import com.dangdang.ddframe.job.config.dataflow.DataflowJobConfiguration;
import com.dangdang.ddframe.job.config.script.ScriptJobConfiguration;
import com.dangdang.ddframe.job.config.simple.SimpleJobConfiguration;
import com.dangdang.ddframe.job.event.rdb.JobEventRdbConfiguration;
import com.dangdang.ddframe.job.executor.handler.JobProperties;
import com.dangdang.ddframe.job.lite.config.LiteJobConfiguration;
import com.dangdang.ddframe.job.lite.spring.api.SpringJobScheduler;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;
import com.maple.rabbit.task.annotation.ElasticJobConfig;
import com.maple.rabbit.task.config.JobZookeeperProperties;
import com.maple.rabbit.task.enums.ElasticJobTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 类描述：解析注解 ElasticJobConfig
 *
 * @author hzc
 * @date 2020/11/28 12:34 下午
 */
@Slf4j
public class ElasticJobConfParser implements ApplicationListener<ApplicationReadyEvent> {

    /**
     * 支持的job类型
     */
    private static final List<String> jobTypeNameList = Arrays.asList(SimpleJob.class.getSimpleName(),
            DataflowJob.class.getSimpleName(), ScriptJob.class.getSimpleName());

    private JobZookeeperProperties jobZookeeperProperties;

    private ZookeeperRegistryCenter zookeeperRegistryCenter;

    public ElasticJobConfParser(JobZookeeperProperties jobZookeeperProperties, ZookeeperRegistryCenter zookeeperRegistryCenter) {
        this.jobZookeeperProperties = jobZookeeperProperties;
        this.zookeeperRegistryCenter = zookeeperRegistryCenter;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {

        try {

            ApplicationContext applicationContext = applicationReadyEvent.getApplicationContext();

            // 获取所有带有此注解的Bean
            Map<String, Object> beanMap = applicationContext.getBeansWithAnnotation(ElasticJobConfig.class);

            for (Map.Entry<String, Object> entry : beanMap.entrySet()) {
                Object confBean = entry.getValue();
                Class<?> cla = confBean.getClass();

                // 若此类带有父类，获取真正的类
                if (cla.getName().indexOf("$") > 0) {
                    String className = cla.getName();
                    cla = Class.forName(className.substring(0, cla.getName().indexOf("$")));
                }

                // 获取继承的接口名
                Class<?>[] interfaces = cla.getInterfaces();

                String jobTypeName = null;
                for (Class<?> interfaceClass : interfaces) {
                    jobTypeName = interfaceClass.getSimpleName();
                    if (jobTypeNameList.contains(jobTypeName)) {
                        break;
                    }
                }

                // 判断注解类是否继承相应的job类型
                if (jobTypeName == null) {
                    log.error("带有此注解 @ElasticJobConfig 的需继承以下接口中的其中一个：{}", jobTypeNameList);
                    System.exit(1);
                }

                // 获取配置项
                ElasticJobConfig conf = cla.getAnnotation(ElasticJobConfig.class);

                // 获取注解中的配置信息
                String jobClass = cla.getName();
                String jobName = this.jobZookeeperProperties.getNamespace() + "." + conf.name();
                String cron = conf.cron();
                String shardingItemParameters = conf.shardingItemParameters();
                String description = conf.description();
                String jobParameter = conf.jobParameter();
                String jobExceptionHandler = conf.jobExceptionHandler();
                String executorServiceHandler = conf.executorServiceHandler();

                String jobShardingStrategyClass = conf.jobShardingStrategyClass();
                String eventTraceRdbDataSource = conf.eventTraceRdbDataSource();
                String scriptCommandLine = conf.scriptCommandLine();

                boolean failover = conf.failover();
                boolean misfire = conf.misfire();
                boolean overwrite = conf.overwrite();
                boolean disabled = conf.disabled();
                boolean monitorExecution = conf.monitorExecution();
                boolean streamingProcess = conf.streamingProcess();

                int shardingTotalCount = conf.shardingTotalCount();
                int monitorPort = conf.monitorPort();
                int maxTimeDiffSeconds = conf.maxTimeDiffSeconds();
                int reconcileIntervalMinutes = conf.reconcileIntervalMinutes();


                JobCoreConfiguration jobCoreConfiguration = JobCoreConfiguration
                        .newBuilder(jobName, cron, shardingTotalCount)
                        .description(description)
                        .jobProperties(JobProperties.JobPropertiesEnum.JOB_EXCEPTION_HANDLER.getKey(), jobExceptionHandler)
                        .jobProperties(JobProperties.JobPropertiesEnum.EXECUTOR_SERVICE_HANDLER.getKey(), executorServiceHandler)
                        .misfire(misfire)
                        .failover(failover)
                        .jobParameter(jobParameter)
                        .shardingItemParameters(shardingItemParameters)
                        .build();

                // 具体要创建的任务
                JobTypeConfiguration jobTypeConfiguration = null;
                if (ElasticJobTypeEnum.SIMPLE.getType().equals(jobTypeName)) {
                    jobTypeConfiguration = new SimpleJobConfiguration(jobCoreConfiguration, jobClass);
                }
                if (ElasticJobTypeEnum.DATAFLOW.getType().equals(jobTypeName)) {
                    jobTypeConfiguration = new DataflowJobConfiguration(jobCoreConfiguration, jobClass, streamingProcess);
                }
                if (ElasticJobTypeEnum.SCRIPT.getType().equals(jobTypeName)) {
                    jobTypeConfiguration = new ScriptJobConfiguration(jobCoreConfiguration, scriptCommandLine);
                }

                LiteJobConfiguration liteJobConfiguration = LiteJobConfiguration.newBuilder(jobTypeConfiguration)
                        .jobShardingStrategyClass(jobShardingStrategyClass)
                        .monitorExecution(monitorExecution)
                        .monitorPort(monitorPort)
                        .maxTimeDiffSeconds(maxTimeDiffSeconds)
                        .overwrite(overwrite)
                        .disabled(disabled)
                        .reconcileIntervalMinutes(reconcileIntervalMinutes)
                        .build();

                // 将生产的类注入到Spring中

                // 创建一个Spring的beanDefinition
                BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.rootBeanDefinition(SpringJobScheduler.class);
                beanDefinitionBuilder.setInitMethodName("init");
                // 设置为多例
                beanDefinitionBuilder.setScope("prototype");

                if (!ElasticJobTypeEnum.SCRIPT.getType().equals(jobTypeName)) {
                    beanDefinitionBuilder.addConstructorArgValue(confBean);
                }

                //new JobEventRdbConfiguration(dataSource);
                beanDefinitionBuilder.addConstructorArgValue(this.zookeeperRegistryCenter);
                beanDefinitionBuilder.addConstructorArgValue(liteJobConfiguration);

                // 若有日志记录事件，则添加
                if (StringUtils.isNotBlank(eventTraceRdbDataSource)) {
                    BeanDefinitionBuilder rdbBuilder = BeanDefinitionBuilder.rootBeanDefinition(JobEventRdbConfiguration.class);
                    Object dataSource = applicationContext.getBean(eventTraceRdbDataSource);
                    rdbBuilder.addConstructorArgValue(dataSource);
                    beanDefinitionBuilder.addConstructorArgValue(rdbBuilder.getBeanDefinition());
                }

                // 添加监听
                List<?> elasticJobListeners = getTargetElasticJobListeners(conf);
                beanDefinitionBuilder.addConstructorArgValue(elasticJobListeners);

                // 放进spring容器中
                DefaultListableBeanFactory factory = (DefaultListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();

                String beanName = conf.name() + "SpringJobScheduler";
                factory.registerBeanDefinition(beanName, beanDefinitionBuilder.getBeanDefinition());

                SpringJobScheduler springJobScheduler = (SpringJobScheduler) applicationContext.getBean(beanName);
                springJobScheduler.init();

                log.info("启动elastic-job作业成功，作业名称：{}", jobName);
            }
            log.info("共计启动成功elastic-job作业数量：{}", beanMap.size());
        } catch (Exception e) {
            e.printStackTrace();
            log.error("elastic-job作业启动失败，系统强制退出", e);
            System.exit(1);
        }
    }

    /**
     * 获取监听的类
     *
     * @param conf
     * @return
     */
    private List<BeanDefinition> getTargetElasticJobListeners(ElasticJobConfig conf) {
        List<BeanDefinition> result = new ManagedList<BeanDefinition>(2);

        String listeners = conf.listener();
        if (StringUtils.isNotBlank(listeners)) {
            BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(listeners);
            builder.setScope("prototype");
            result.add(builder.getBeanDefinition());
        }
        String distributedListeners = conf.distributedListener();
        long startedTimeoutMilliseconds = conf.startedTimeoutMilliseconds();
        long completedTimeoutMilliseconds = conf.completedTimeoutMilliseconds();
        if (StringUtils.isNotBlank(distributedListeners)) {
            BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(distributedListeners);
            factory.setScope("prototype");
            factory.addConstructorArgValue(Long.valueOf(startedTimeoutMilliseconds));
            factory.addConstructorArgValue(Long.valueOf(completedTimeoutMilliseconds));
            result.add(factory.getBeanDefinition());
        }
        return result;
    }


}
