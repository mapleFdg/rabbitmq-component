package com.maple.rabbit.esjob.config;

import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperConfiguration;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * 类描述：注册到 Zookeeper容器 上
 *
 * @author hzc
 * @date 2020/11/25 11:34 下午
 */
//@Configuration
//@ConditionalOnExpression("'${zookeeper.address}'.length() > 0")
public class RegistryCenterConfig {

    @Value("${zookeeper.address}")
    private String address;
    @Value("${zookeeper.namespace}")
    private String namespace;
    @Value("${zookeeper.connectionTimeout}")
    private int connectionTimeout;
    @Value("${zookeeper.sessionTimeout}")
    private int sessionTimeout;
    @Value("${zookeeper.maxRetries}")
    private int maxRetries;

    /**
     * 把注册中心加载到spring 容器中
     *
     * @return
     */
    @Bean(initMethod = "init",name = "registryCenter")
    public ZookeeperRegistryCenter registryCenter(){
        ZookeeperConfiguration zookeeperConfiguration = new ZookeeperConfiguration(address , namespace);
        zookeeperConfiguration.setConnectionTimeoutMilliseconds(connectionTimeout);
        zookeeperConfiguration.setSessionTimeoutMilliseconds(sessionTimeout);
        zookeeperConfiguration.setMaxRetries(maxRetries);
        return new ZookeeperRegistryCenter(zookeeperConfiguration);
    }


}
