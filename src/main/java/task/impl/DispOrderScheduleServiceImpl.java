package com.honyicare.urp.manager.task.impl;

import com.alibaba.fastjson.JSONObject;
import com.honyicare.urp.common.utils.LogUtil;
import com.honyicare.urp.manager.common.utils.BaseConfigUtils;
import com.honyicare.urp.manager.common.utils.StringUtil;
import com.honyicare.urp.manager.entity.OrderSchedulePart;
import com.honyicare.urp.manager.entity.OrderScheduleRespool;
import com.honyicare.urp.manager.entity.OrderScheduleRespoolExample;
import com.honyicare.urp.manager.enums.DictHospOrderResType;
import com.honyicare.urp.manager.enums.DictHospScheduleStatus;
import com.honyicare.urp.manager.repository.OrderScheduleRespoolMapper;
import com.honyicare.urp.manager.repository.ext.OrderScheduleRespoolExtMapper;
import com.honyicare.urp.manager.task.TaskService;
import com.honyicare.urp.manager.task.processor.CreateScheduleProcessor;
import com.honyicare.urp.manager.task.support.DispOrderScheduleService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.util.*;

/**
 * @author hzc
 * @date 2020/10/18 11:50 上午
 */
@Service
public class DispOrderScheduleServiceImpl extends CreateScheduleProcessor implements DispOrderScheduleService {

    private static final Logger log = LogUtil.getLogger();

    @Autowired
    private OrderScheduleRespoolExtMapper orderScheduleRespoolExtMapper;

    @Autowired
    private OrderScheduleRespoolMapper orderScheduleRespoolMapper;

    @Autowired
    private TaskService taskService;

    @Override
    @Transactional(propagation = Propagation.REQUIRED,rollbackFor=Exception.class)
    public boolean genarateRespool(List<OrderSchedulePart> orderScheduleParts,String generateStatus,String userUuid) {
        try {
            String configQueueNo = BaseConfigUtils.getParam(CreateScheduleProcessor.CONFIG_CURRENT_QUEUE_NO);
            if(orderScheduleParts == null || orderScheduleParts.isEmpty()){
                log.error("orderScheduleParts array is empty!");
                return false;
            }

            String orgUuid = orderScheduleParts.get(0).getOrgCode();
            String orgDeptUuid = orderScheduleParts.get(0).getOrgDeptCode();
            String orgDpetEmpUuid = orderScheduleParts.get(0).getOrgDeptEmpUuid();
            Date sdate = orderScheduleParts.get(0).getSdate();
            String timePartType = orderScheduleParts.get(0).getTimePartType();

            // 删除此医生该午别的号源，重新生成
            OrderScheduleRespool orderScheduleRespoolForUpdate = new OrderScheduleRespool();
            orderScheduleRespoolForUpdate.setStatus(DictHospScheduleStatus.DELETE.key);
            orderScheduleRespoolForUpdate.setUpdateTime(new Date());
            orderScheduleRespoolForUpdate.setUpdateUserUuid(userUuid);
            OrderScheduleRespoolExample example = new OrderScheduleRespoolExample();
            OrderScheduleRespoolExample.Criteria criteria = example.createCriteria();
            criteria.andOrgCodeEqualTo(orgUuid);
            criteria.andOrgDeptCodeEqualTo(orgDeptUuid);
            criteria.andOrgDeptEmpUuidEqualTo(orgDpetEmpUuid);
            criteria.andTimePartTypeEqualTo(timePartType);
            criteria.andSdateEqualTo(sdate);
            criteria.andStatusEqualTo(DictHospScheduleStatus.APPROVING.key);
            orderScheduleRespoolMapper.updateByExampleSelective(orderScheduleRespoolForUpdate,example);


            /**
             * 按timeBegin排序
             */
            orderScheduleParts.sort((t1, t2)-> {
                if(compareTimeBegin(t1.getTimeBegin(),t2.getTimeBegin())){
                    return 1;
                }else{
                    return -1;
                }
            });

            Long yuyueStart;
            Long todayStart;

            if(StringUtils.isBlank(configQueueNo) || "2".equals(configQueueNo)){
                // 当日号排序开始
                todayStart = orderScheduleParts.stream()
                        .mapToInt((s) -> s.getAppoTotal()).summaryStatistics().getSum() + 1;
                // 预约号排序开始
                yuyueStart = 1L;
            }else{
                // 当日号排序开始
                yuyueStart = orderScheduleParts.stream()
                        .mapToInt((s) -> s.getCurrentTotal()).summaryStatistics().getSum() + 1;
                // 预约号排序开始
                todayStart = 1L;
            }

            Integer maxSerialNum;

            // 获取当日此医院号源编号的最大值
            String serialStr = orderScheduleRespoolExtMapper.getMaxSerialWithOrgCodeAndSdate(orgUuid, sdate);
            if(StringUtils.isBlank(serialStr)){
                serialStr = "20102100000";
            }
            maxSerialNum = Integer.parseInt(StringUtils.substring(serialStr,6));
            Integer deptStartNum = maxSerialNum + 1;

            List<OrderScheduleRespool> insertOSRdata = new ArrayList<>();

            // 生成时间段号源
            for (OrderSchedulePart orderSchedulePart : orderScheduleParts) {
                //当日号
                Integer currentTotal = orderSchedulePart.getCurrentTotal();
                for (int i = 1; i <= currentTotal; i++) {
                    // 生成当日号号源
                    OrderScheduleRespool orderScheduleRespool = genarateRespool(orderSchedulePart, deptStartNum,
                            DictHospOrderResType.SCENE.key,todayStart,generateStatus);
                    deptStartNum++;
                    todayStart++;
                    insertOSRdata.add(orderScheduleRespool);
                }

                // 预约号
                Integer appoTotal = orderSchedulePart.getAppoTotal();
                for (int i = 1; i <= appoTotal; i++) {
                    // 生成预约号号源
                    OrderScheduleRespool orderScheduleRespool = genarateRespool(orderSchedulePart, deptStartNum,
                            DictHospOrderResType.ONLINE.key,yuyueStart,generateStatus);
                    deptStartNum++;
                    yuyueStart++;
                    insertOSRdata.add(orderScheduleRespool);
                }
            }
            orderScheduleRespoolExtMapper.insertOrderScheduleRespools(insertOSRdata);
        } catch (Exception e) {
            log.error("生成号源异常: " + e.getMessage(),e);
            throw new RuntimeException(e);
        }
        return false;
    }


    @Override
    public void updateTaskNextTime(String botUuid) {
        String time = BaseConfigUtils.getParam(CREATE_SCHEDULE_TIME);
        String week = BaseConfigUtils.getParam(CREATE_SCHEDULE_WEEK);
        if (StringUtils.isBlank(week)) {
            return;
        }
        Date nextWeek = getNextWeek(new Date(), Integer.parseInt(week));
        String nextWeekStr = DateFormatUtils.format(nextWeek, "yyyy-MM-dd");
        Date nextTime;
        try {
            nextTime = DateUtils.parseDate(nextWeekStr + " " + time, "yyyy-MM-dd HH:mm");
        } catch (ParseException e) {
            nextTime = nextWeek;
        }
        taskService.updateNextTime(botUuid, nextTime);
    }

    /**
     * 获取下周日期
     *
     * @param date
     * @param week
     * @return
     */
    private Date getNextWeek(Date date, Integer week) {
        Integer weekNum;
        if (week == 7) {
            weekNum = 1;
        } else {
            weekNum = week + 1;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        Integer dayNum = cal.get(Calendar.DAY_OF_WEEK);
        if (dayNum >= weekNum) {
            cal.add(Calendar.WEEK_OF_YEAR, 1);
        }
        cal.set(Calendar.DAY_OF_WEEK, weekNum);
        return cal.getTime();
    }
}
