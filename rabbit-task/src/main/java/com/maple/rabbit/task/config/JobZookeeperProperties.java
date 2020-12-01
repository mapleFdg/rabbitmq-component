package com.maple.rabbit.task.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 类描述：elastic.job.zk 配置类，包含zk的相关配置
 *
 * @author hzc
 * @date 2020/11/26 10:58 下午
 */
@Data
@ConfigurationProperties(prefix = "elastic.job.zk")
public class JobZookeeperProperties {

    /**
     * 连接 ZooKeeper 服务器的列表
     */
    private String namespace;

    /**
     * ZooKeeper 的命名空间
     */
    private String serverLists;

    /**
     * 最大重试次数
     */
    private int maxRetries = 3;

    /**
     * 连接超时毫秒数
     */
    private int connectionTimeoutMilliseconds = 15000;

    /**
     * 会话超时毫秒数
     */
    private int sessionTimeoutMilliseconds = 60000;

    /**
     * 	等待重试的间隔时间的初始毫秒数
     */
    private int baseSleepTimeMilliseconds = 1000;

    /**
     * 等待重试的间隔时间的最大毫秒数
     */
    private int maxSleepTimeMilliseconds = 3000;

    /**
     * 连接 ZooKeeper 的权限令牌
     */
    private String digest = "";

}
