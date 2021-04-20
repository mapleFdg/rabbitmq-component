package com.maple.rabbit.producer.broker;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

/**
 * 类描述：消息发送异步队列
 *
 * @author hzc
 * @date 2020/11/22 1:21 下午
 */
@Slf4j
public class AsyncBaseQueue {

    /**
     * 线程大小，Java虚拟机可用的处理器数量。
     *
     * the number of processors available to the Java virtual machine
     */
    private static final int THREAD_SIZE  = Runtime.getRuntime().availableProcessors();

    /**
     * 队列大小
     * Queue size
     */
    private static final int QUEUE_SIZE = 1000;

    /**
     * 发送调用线程池
     */
    private static ExecutorService senderAsync = new ThreadPoolExecutor(THREAD_SIZE, THREAD_SIZE, 60L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(QUEUE_SIZE),
            new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r);
                    t.setName("rabbitmq_client_async_sender");
                    return t;
                }
            }, new RejectedExecutionHandler() {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            log.error("async sender is error rejected, runnable : {} , excutor : {}",r,executor);
        }
    });

    public static void submit(Runnable r){
        senderAsync.submit(r);
    }
}
