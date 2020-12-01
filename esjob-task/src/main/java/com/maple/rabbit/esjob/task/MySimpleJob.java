package com.maple.rabbit.esjob.task;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.simple.SimpleJob;
import com.maple.rabbit.esjob.listener.SimpleJobListener;
import com.maple.rabbit.task.annotation.ElasticJobConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 类描述：自定义任务
 *
 * @author hzc
 * @date 2020/11/25 11:54 下午
 */
@ElasticJobConfig(
        name = "mySimpleJob",
        cron = "0/5 * * * * ?",
        shardingTotalCount = 3,
        listener = "com.maple.rabbit.esjob.listener.SimpleJobListener",
        eventTraceRdbDataSource = "dataSource"
)
@Component
@Slf4j
public class MySimpleJob implements SimpleJob {

    @Override
    public void execute(ShardingContext shardingContext) {
        log.info("开始任务-----");
    }
}
