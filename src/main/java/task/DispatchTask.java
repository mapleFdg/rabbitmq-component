package com.honyicare.urp.manager.task;
/**
 * @author hzc
 * @date 2020-07-31 23:22
 */

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.honyicare.urp.common.utils.LogUtil;
import com.honyicare.urp.manager.entity.BaseOrgTask;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.net.InetAddress;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.Future;

/**
 *
 * 定时调度任务
 *
 * @author hzc
 * @date 2020-07-31 23:22
 */
@Component
public class DispatchTask {

    private static final Logger log = LogUtil.getLogger();

    @Resource(name = "dispTaskService")
    private TaskService taskService;

    @Autowired
    private Map<String, TaskProcessor> taskProcessors;

    @Autowired
    private ThreadPoolTaskExecutor threadPool;

    /**
     * 扫描定时任务，默认30秒一次（可在配置文件中配置）
     *
     * 1、查询可执行的任务
     * 2、锁定任务，并修改下次执行时间
     * 3、锁定失败，重新查询进行锁定
     * 4、锁定成功，增加任务执行详情，将任务放进执行队列，进入等待执行状态
     * 5、任务开始执行，修改状态为执行状态
     * 6、任务执行结束
     *         - 成功，修改状态为执行成功状态，并释放任务锁
     *         - 失败，修改状态为失败的状态，并记录失败原因到详情中，（是否需要发送通知？）
     *
     *
     */
    @Scheduled(cron = "*/30 * * * * ?")
    public void scanTask() throws ParseException {
        log.debug("【定时任务调度】扫描可执行任务");
        Thread current = Thread.currentThread();
        String computerLocalInfo = getComputerInfo() + "_" + current.getId();
        Date nowTime = DateUtils.parseDate(DateFormatUtils.format(new Date(),"yyyy-MM-dd HH:mm:ss"),
                "yyyy-MM-dd HH:mm:ss");

        // 锁定任务
        Integer lockNum = taskService.lockTask(computerLocalInfo,nowTime);

        log.info("【定时任务调度】锁定任务数：" + lockNum);

        if(lockNum < 1){
            log.info("【定时任务调度】无可执行任务");
            return ;
        }

        /**
         * 获取当前锁定的任务
         */
        List<BaseOrgTask> lockTasks = taskService.getLockTask(computerLocalInfo, nowTime);

        /**
         * 执行完成与执行中的任务线程
         */
        Map<String,List<Future<Integer>>> futureMap = new HashMap<>();
        /**
         * 任务参数
         */
        Map<String,JSONArray> paramMap = new HashMap<>();
        /**
         * 任务详情的ID
         */
        Map<String,String> detailIdMap = new HashMap<>();

        /**
         * 构建任务参数，
         */
        for(BaseOrgTask task : lockTasks){
            String functionName = task.getFunctionName();
            String botUuid = task.getBotUuid();
            /**
             * 每个任务的唯一KEY值
             */
            String functionKey = functionName + botUuid;
            log.info("【定时任务调度】【"+functionName+"】构建参数");
            TaskProcessor taskProcessor = taskProcessors.get(functionName);
            JSONArray paramArray = taskProcessor.buildParam(task);
            paramMap.put(functionKey,paramArray);
            log.info("【定时任务调度】【"+functionKey+"】务可以正常执行，总共分解为" + paramArray.size() + "个子任务");
        }

        /**
         * 循环遍历任务数组，直到每个任务都完成并移除任务数组
         */
        for (int cycleIndex = 0; lockTasks.size() > 0; cycleIndex++){

            for(int taskIndex = 0;taskIndex < lockTasks.size(); taskIndex++){
                BaseOrgTask task = lockTasks.get(taskIndex);
                String functionName = task.getFunctionName();
                String botUuid = task.getBotUuid();
                String functionKey = functionName + botUuid;
                JSONArray paramArray = paramMap.get(functionKey);

                List<Future<Integer>> futureList = futureMap.get(functionName);

                // futureList为空，则为首次遍历，初始化futureList，并插入任务详情记录
                if(futureList == null){
                    futureList = new ArrayList<>();
                    futureMap.put(functionName,futureList);
                    // 插入任务详情
                    String botrUuid = taskService.addDetail(task);
                    detailIdMap.put(functionKey,botrUuid);
                }

                if(cycleIndex > 0){
                    // 非首次循环，判断任务是否已执行完成。

                    boolean isFinish = false;

                    if((futureList.isEmpty())
                            && (paramArray == null || paramArray.isEmpty())){
                        // 任务线程组为空(无任务需要执行)且无任务参数(所有参数都已执行)
                        isFinish = true;
                    }else if(paramArray == null || paramArray.isEmpty()){
                        // 任务线程组不为空，任务参数为空(所有参数都已执行)
                        isFinish = true;
                        // 判断任务线程是否已执行完成
                        for(Future<Integer> future : futureList){
                            if(!future.isDone()){
                                isFinish = false;
                                break;
                            }
                        }
                    }

                    // 任务已完成，更新任务状态
                    if(isFinish){

                        String botrUuid = detailIdMap.get(functionKey);
                        // 更新任务详情的结束时间
                        if(StringUtils.isNotBlank(botrUuid)){
                            taskService.updateDetailEndTime(botrUuid);
                            detailIdMap.remove(functionKey);
                        }
                        // 解锁任务
                        taskService.unlockTask(task.getBotUuid());
                        // 从任务列表中移除
                        lockTasks.remove(taskIndex);
                        taskIndex--;
                        log.info("【定时任务调度】【"+functionKey+"】任务执行成功");
                        continue;
                    }
                }else{
                    log.info("【定时任务调度】执行任务：" + JSONObject.toJSONString(task));
                }

                // 参数为空，不需要执行
                if(paramArray == null){
                    continue;
                }

                // 最大线程数，若为空默认1
                Integer processNumber = task.getProcessNumber();
                if(processNumber == null){
                    processNumber = 1;
                }

                // 最大线程数少于1，不执行
                if(processNumber < 1){
                    log.info("【定时任务调度】【"+functionKey+"】执行最大线程数为0！");
                    paramMap.remove(functionKey);
                    continue;
                }

                // 参数为空，不执行
                if(paramArray.isEmpty()){
                    paramMap.remove(functionKey);
                    continue;
                }

                TaskProcessor taskProcessor = taskProcessors.get(functionName);

                for(int i = 0; i < paramArray.size(); i++){
                    JSONObject param = paramArray.getJSONObject(i);

                    /**
                     * 查看是否有可用线程
                     */
                    int runTaskNum = 0;
                    for(Future<Integer> futureTask : futureList){
                        if (!futureTask.isDone()) {
                            runTaskNum = runTaskNum + 1;
                        }
                    }
                    if (runTaskNum >= processNumber) {
                        break;
                    }

                    // 执行任务
                    Future<Integer> future = threadPool.submit(() -> {
                        log.info("【定时任务调度】【"+functionKey+"】开始执行，参数：\n" + JSONObject.toJSONString(param,true) );
                        try {
                            taskProcessor.taskRun(task,param);
                        } catch (Exception e) {
                            log.error("【定时任务调度】【"+functionKey+"】执行失败，错误信息：" + e.getMessage(),e);
                            return 0;
                        }
                        log.info("【定时任务调度】【"+functionKey+"】执行成功" );
                        return 1;
                    });

                    // 任务已执行，移除参数
                    paramArray.remove(i);
                    i--;
                    futureList.add(future);
                }
            }
        }
    }

    /**
     * 获取服务器信息
     *
     * @return
     */
    private String getComputerInfo() {
        InetAddress addr;
        String ip = "";
        String hostName = "";
        try {
            addr = InetAddress.getLocalHost();
            ip = addr.getHostAddress();// 获得本机IP
            hostName = addr.getHostName();// 获得本机名称
        } catch (Exception e) {
            e.printStackTrace();
        }
        return hostName + "(" + ip + ")";
    }
}
