package com.maple.rabbit.producer.task;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.dataflow.DataflowJob;
import com.maple.rabbit.producer.entity.BrokerMessage;
import com.maple.rabbit.task.annotation.ElasticJobConfig;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 类描述：
 *
 * @author hzc
 * @date 2020/12/1 11:38 下午
 */
@Component
@ElasticJobConfig(
        name = "com.maple.rabbit.producer.task.RetryMessageDataflowJob",
        cron = "*/10 * * * * ?",
        shardingTotalCount = 1,
        eventTraceRdbDataSource = "dataSource"
)
public class RetryMessageDataflowJob implements DataflowJob<BrokerMessage> {

    @Override
    public List<BrokerMessage> fetchData(ShardingContext shardingContext) {
        return null;
    }

    @Override
    public void processData(ShardingContext shardingContext, List<BrokerMessage> data) {

    }
}
