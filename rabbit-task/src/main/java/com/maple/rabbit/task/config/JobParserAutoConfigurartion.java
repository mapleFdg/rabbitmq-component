package com.maple.rabbit.task.config;

import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperConfiguration;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;
import com.maple.rabbit.task.parser.ElasticJobConfParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 类描述：esJob自动装配类 连接zk
 *
 * @author hzc
 * @date 2020/11/26 10:56 下午
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "elastic.job.zk", name = {"namespace", "serverLists"})
@EnableConfigurationProperties(JobZookeeperProperties.class)
public class JobParserAutoConfigurartion {

    /**
     * 配置zk
     *
     * @param jobZookeeperProperties
     * @return
     */
    @Bean(initMethod = "init")
    public ZookeeperRegistryCenter zookeeperRegistryCenter(JobZookeeperProperties jobZookeeperProperties) {
        ZookeeperConfiguration configuration = new ZookeeperConfiguration(jobZookeeperProperties.getServerLists(), jobZookeeperProperties.getNamespace());
        configuration.setDigest(jobZookeeperProperties.getDigest());
        configuration.setMaxRetries(jobZookeeperProperties.getMaxRetries());
        configuration.setSessionTimeoutMilliseconds(jobZookeeperProperties.getSessionTimeoutMilliseconds());
        configuration.setConnectionTimeoutMilliseconds(jobZookeeperProperties.getConnectionTimeoutMilliseconds());
        configuration.setBaseSleepTimeMilliseconds(jobZookeeperProperties.getBaseSleepTimeMilliseconds());
        configuration.setMaxSleepTimeMilliseconds(jobZookeeperProperties.getMaxSleepTimeMilliseconds());

        log.info("es-job zookeeper connection successful! serverList: {} , namespace: {}", jobZookeeperProperties.getServerLists(), jobZookeeperProperties.getNamespace());
        return new ZookeeperRegistryCenter(configuration);
    }

    /**
     * 声明ElasticJobConfParser Bean
     *
     * @param jobZookeeperProperties
     * @param zookeeperRegistryCenter
     * @return
     */
    @Bean
    public ElasticJobConfParser elasticJobConfParser(JobZookeeperProperties jobZookeeperProperties, ZookeeperRegistryCenter zookeeperRegistryCenter) {
        return new ElasticJobConfParser(jobZookeeperProperties, zookeeperRegistryCenter);
    }
}
