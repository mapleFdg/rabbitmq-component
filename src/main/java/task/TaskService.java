package com.honyicare.urp.manager.task;

import com.honyicare.urp.manager.entity.BaseOrgTask;

import java.util.Date;
import java.util.List;

/**
 * 扫描任务Service类
 *
 * @author hzc
 * @date 2020-08-22 16:14
 */
public interface TaskService {

    /**
     * 锁定任务
     */
    Integer lockTask(String machineCode, Date currentTime);

    void unlockTask(String botUuid);

    List<BaseOrgTask> getLockTask(String machineCode, Date currentTime);

    void doTask(BaseOrgTask task);

    String addDetail(BaseOrgTask task);

    void updateDetailEndTime(String botrUuid);

    void updateNextTime(String botUuid,Date nextTime);

}
