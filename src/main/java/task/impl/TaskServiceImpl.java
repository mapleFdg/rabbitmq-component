package com.honyicare.urp.manager.task.impl;

import com.honyicare.urp.manager.entity.BaseOrgTask;
import com.honyicare.urp.manager.entity.BaseOrgTaskExample;
import com.honyicare.urp.manager.entity.BaseOrgTaskRecord;
import com.honyicare.urp.manager.enums.DictPubStatus;
import com.honyicare.urp.manager.repository.BaseOrgTaskMapper;
import com.honyicare.urp.manager.repository.BaseOrgTaskRecordMapper;
import com.honyicare.urp.manager.repository.ext.BaseOrgTaskExtMapper;
import com.honyicare.urp.manager.task.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @author hzc
 * @date 2020-08-22 16:14
 */
@Service("dispTaskService")
public class TaskServiceImpl implements TaskService {

    @Autowired
    private BaseOrgTaskExtMapper baseOrgTaskExtMapper;

    @Autowired
    private BaseOrgTaskMapper baseOrgTaskMapper;

    @Autowired
    private BaseOrgTaskRecordMapper baseOrgTaskRecordMapper;

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Integer lockTask(String machineCode, Date currentTime) {
        /**
         * 1、查询当前执行的任务数量 若小于 MAX_RUNNING_SIZE ， 则进行任务的锁定，否则不锁定任务；
         * 2、查看可锁定的任务数量
         *
         */
        Map<String,Object> lockParam = new HashMap<>();
        lockParam.put("machineCode",machineCode);
        lockParam.put("lockDate",currentTime);
        lockParam.put("updateTime",currentTime);
        return baseOrgTaskExtMapper.updateForLock(lockParam);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void unlockTask(String botUuid){
        Map<String,Object> unlockParam = new HashMap<>();
        unlockParam.put("botUuid",botUuid);
        unlockParam.put("updateTime",new Date());
        Integer unlockNum = baseOrgTaskExtMapper.updateForUnlock(unlockParam);
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public List<BaseOrgTask> getLockTask(String machineCode, Date currentTime) {
        BaseOrgTaskExample example = new BaseOrgTaskExample();
        BaseOrgTaskExample.Criteria criteria = example.createCriteria();
        criteria.andCurrentMachineCodeEqualTo(machineCode);
        criteria.andCurrentBeginTimeEqualTo(currentTime);
        criteria.andStatusEqualTo(DictPubStatus.NORMAL.key);
        return baseOrgTaskMapper.selectByExample(example);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void doTask(BaseOrgTask task) {

    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public String addDetail(BaseOrgTask task){
        BaseOrgTaskRecord record = new BaseOrgTaskRecord();
        record.setTaskUuid(task.getBotUuid());
        record.setFunctionName(task.getFunctionName());
        record.setFunctionCode(task.getFunctionCode());
        record.setOrgCode(task.getOrgCode());
        record.setBeginTime(new Date());
        record.setMachineCode(task.getCurrentMachineCode());
        record.setStatus(DictPubStatus.NORMAL.key);
        record.setUpdateTime(new Date());
        record.setUpdateUserUuid("1");
        record.setCreateTime(new Date());
        record.setCreateUserUuid("1");
        baseOrgTaskRecordMapper.insertSelective(record);
        return record.getBotrUuid();
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void updateDetailEndTime(String botrUuid){
        BaseOrgTaskRecord record = new BaseOrgTaskRecord();
        record.setBotrUuid(botrUuid);
        record.setEndTime(new Date());
        record.setUpdateTime(new Date());
        record.setUpdateUserUuid("1");
        baseOrgTaskRecordMapper.updateByPrimaryKeySelective(record);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void updateNextTime(String botUuid,Date nextTime){
        BaseOrgTask task = new BaseOrgTask();
        task.setBotUuid(botUuid);
        task.setNextTime(nextTime);
        baseOrgTaskMapper.updateByPrimaryKeySelective(task);
    }
}
