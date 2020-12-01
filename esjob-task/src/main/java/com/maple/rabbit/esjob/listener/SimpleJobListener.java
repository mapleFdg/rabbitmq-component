package com.maple.rabbit.esjob.listener;

import com.dangdang.ddframe.job.executor.ShardingContexts;
import com.dangdang.ddframe.job.lite.api.listener.ElasticJobListener;
import lombok.extern.slf4j.Slf4j;

/**
 * 类描述： 时间开始与结束的监听
 *
 * @author hzc
 * @date 2020/11/25 11:58 下午
 */
@Slf4j
public class SimpleJobListener implements ElasticJobListener {

    @Override
    public void beforeJobExecuted(ShardingContexts shardingContexts) {
        log.info("任务开始");
    }

    @Override
    public void afterJobExecuted(ShardingContexts shardingContexts) {
        log.info("任务结束");
    }
}
