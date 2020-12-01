package com.maple.rabbit.task.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 注解描述：配置任务相关信息
 *
 * @author hzc
 * @date 2020/11/26 11:12 下午
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ElasticJobConfig {

    /**  作业名称 */
    String name();

    /**  CRON 表达式，用于控制作业触发时间 */
    String cron() default "";
    /**  作业分片总数 */
    int shardingTotalCount() default 1;
    /**  个性化分片参数 */
    String shardingItemParameters() default "";
    /**  作业自定义参数 */
    String jobParameter() default "";
    /**  是否开启任务执行失效转移 */
    boolean failover() default false;
    /**  是否开启错过任务重新执行 */
    boolean misfire() default true;
    /**  作业名称 */
    String description() default "";

    boolean overwrite() default false;

    boolean streamingProcess() default false;

    String scriptCommandLine() default "";
    /** 监控作业运行时状态 */
    boolean monitorExecution() default false;

    public int monitorPort() default -1;	//must
    /** 最大允许的本机与注册中心的时间误差秒数 */
    public int maxTimeDiffSeconds() default -1;	//must

    public String jobShardingStrategyClass() default "";	//must
    /** 修复作业服务器不一致状态服务调度间隔分钟 */
    public int reconcileIntervalMinutes() default 10;	//must

    /** 数据源的bean名称 */
    public String eventTraceRdbDataSource() default "";	//must

    public String listener() default "";	//must

    public boolean disabled() default false;	//must

    public String distributedListener() default "";

    public long startedTimeoutMilliseconds() default Long.MAX_VALUE;	//must

    public long completedTimeoutMilliseconds() default Long.MAX_VALUE;		//must

    public String jobExceptionHandler() default "com.dangdang.ddframe.job.executor.handler.impl.DefaultJobExceptionHandler";

    public String executorServiceHandler() default "com.dangdang.ddframe.job.executor.handler.impl.DefaultExecutorServiceHandler";
}
