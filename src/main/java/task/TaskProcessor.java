package com.honyicare.urp.manager.task;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.honyicare.urp.manager.entity.BaseOrgTask;

/**
 * 任务执行类
 *
 * @author hzc
 * @date 2020-08-22 16:24
 */
public interface TaskProcessor {

    /**
     * 构建任务参数
     *
     * @return
     */
    public JSONArray buildParam(BaseOrgTask task);

    /**
     * 执行任务
     *
     * @return
     */
    public JSONObject taskRun(BaseOrgTask task,JSONObject paramJson) throws Exception;

    /**
     * 结果处理
     *
     * @return
     */
    public JSONObject resultDeal();

}
