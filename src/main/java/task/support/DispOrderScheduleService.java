package com.honyicare.urp.manager.task.support;

import com.honyicare.urp.manager.entity.OrderSchedulePart;


import java.util.List;

/**
 * 定时任务号源相关业务类
 *
 * @author hzc
 * @date 2020/10/18 11:48 上午
 */
public interface DispOrderScheduleService {

    /**
     * 生成号源表
     *
     * @param orderSchedulePart
     * @return
     */
    public boolean genarateRespool(List<OrderSchedulePart> orderSchedulePart,String generateStatus,String userUuid);

    /**
     * 更新生成排班定时任务的下次执行时间
     *
     * @param botUuid
     */
    public void updateTaskNextTime(String botUuid);

}
