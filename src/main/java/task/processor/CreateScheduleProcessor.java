package com.honyicare.urp.manager.task.processor;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.honyicare.urp.common.utils.LogUtil;
import com.honyicare.urp.manager.common.utils.BaseConfigUtils;
import com.honyicare.urp.manager.entity.*;
import com.honyicare.urp.manager.entity.ext.OrderScheduleTpWithPart;
import com.honyicare.urp.manager.enums.*;
import com.honyicare.urp.manager.repository.BaseOrgCalendarMapper;
import com.honyicare.urp.manager.repository.ext.*;
import com.honyicare.urp.manager.rule.RuleService;
import com.honyicare.urp.manager.rule.support.RuleConstant;
import com.honyicare.urp.manager.rule.support.RuleResult;
import com.honyicare.urp.manager.rule.vo.InitialSchedu;
import com.honyicare.urp.manager.rule.vo.OnceScheduleWeeks;
import com.honyicare.urp.manager.task.TaskProcessor;
import com.honyicare.urp.manager.task.TaskService;
import com.honyicare.urp.manager.task.support.DispOrderScheduleService;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.*;

/**
 * @author hzc
 * @date 2020-09-18 00:04
 */
@Service("CREATE_SCHEDULE")
public class CreateScheduleProcessor implements TaskProcessor {

    private static final Logger log = LogUtil.getLogger();

    /**
     * 参数KEY值
     */
    private static final String PARAM_ORG_UUID = "orgUuid";
    private static final String PARAM_ORG_DEPT_UUID = "orgDeptUuid";
    private static final String PARAM_SCHDEULE_DATE = "schdeuleDate";
    private static final String PARAM_SCHEDULE_WEEKS = "scheduleWeeks";
    private static final String PARAM_DEPT_MAX_NUM = "deptMaxNum";
    private static final String PARAM_DEPT_START_NUM = "deptStartNum";

    private static final String PARAM_CALENDAR_IS_EXCHANGE = "isExchange";
    private static final String PARAM_CALENDAR_WORK_OR_HOLIDAY = "workOrHoliday";
    private static final String PARAM_CALENDAR_DAY = "day";

    /**
     * 配置表里的key值
     */
    public static final String CREATE_SCHEDULE_WEEK = "CREATE_SCHEDULE_WEEK";
    public static final String CREATE_SCHEDULE_TIME = "CREATE_SCHEDULE_TIME";
    public static final String CONFIG_DEPT_MAX_NUMS = "CONFIG_DEPT_MAX_NUMS";
    public static final String CONFIG_CURRENT_QUEUE_NO = "CONFIG_CURRENT_QUEUE_NO";

    public static final String CONFIG_DEPT_MAX_NUMS_DEFAULT = "%s_default";
    public static final String CONFIG_DEPT_MAX_NUMS_KEY = "%s_%s";
    public static final String STOP_TIME_KEY = "%s_%s";

    public static final String DEFAULT_USER_ID =  "0";

    @Autowired
    private OrderScheduleTpExtMapper orderScheduleTpExtMapper;

    @Autowired
    private OrderScheduleExtMapper orderScheduleExtMapper;

    @Autowired
    private OrderSchedulePartExtMapper orderSchedulePartExtMapper;

    @Autowired
    private OrderScheduleRespoolExtMapper orderScheduleRespoolExtMapper;

    @Autowired
    private BaseOrgCalendarMapper baseOrgCalendarMapper;

    @Autowired
    private RuleService ruleService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private StopReplaceApplyExtMapper stopReplaceApplyExtMapper;

    @Autowired
    private DispOrderScheduleService dispOrderScheduleService;

    @Override
    public JSONArray buildParam(BaseOrgTask task) {
        // 更新定时任务的下次执行时间
        dispOrderScheduleService.updateTaskNextTime(task.getBotUuid());

        /**
         * 组装参数
         * 以科室为维度
         * 加上初始排班时间、号源开始编号与编号数、排班周数
         *
         */
        JSONArray array = new JSONArray();

        /**
         * 查询排班模板中科室ID与医院ID
         */
        List<Map<String, Object>> maps = orderScheduleTpExtMapper.selectForCreateScheduleParam(task.getOrgCode());

        /**
         * 获取配置中科室最大号源数
         */
        String deptMaxNumsStr = BaseConfigUtils.getParam(CONFIG_DEPT_MAX_NUMS);
        JSONObject deptMaxNums = JSONObject.parseObject(deptMaxNumsStr);


        // 当前编号排序起始位置
        Integer orderNum = 0;


        for (Map<String, Object> map : maps) {
            JSONObject paramJson = new JSONObject();
            String orgDeptUuid = map.get(PARAM_ORG_DEPT_UUID) == null ? "" : map.get(PARAM_ORG_DEPT_UUID).toString();

            Integer deptMaxNum = deptMaxNums.getInteger(String.format(CONFIG_DEPT_MAX_NUMS_KEY,task.getOrgCode(),orgDeptUuid));
            if(deptMaxNum == null){
                deptMaxNum = deptMaxNums.getInteger(String.format(CONFIG_DEPT_MAX_NUMS_DEFAULT,task.getOrgCode()));
            }

            // 查询规则
            JSONObject ruleParam = new JSONObject();
            ruleParam.put(PARAM_ORG_UUID, task.getOrgCode());
            ruleParam.put(PARAM_ORG_DEPT_UUID, orgDeptUuid);

            /**
             * 初次排班时间规则
             */
            RuleResult<InitialSchedu> ruleResult = ruleService.check(RuleConstant.INITIAL_SCHEDU, ruleParam);
            if (ruleResult != null && ruleResult.getCode() == 0 && ruleResult.getResult()) {
                String schdeuleDate = ruleResult.getData().getDate();
                try {
                    Date date = DateUtils.parseDate(schdeuleDate, "yyyy-MM-dd HH:mm:ss");
                    if (date.getTime() > System.currentTimeMillis()) {
                        // 未到初次排班时间，不执行此科室排班
                        continue;
                    }
                } catch (ParseException e) {
                    // nothing to do
                }
                paramJson.put(PARAM_SCHDEULE_DATE, schdeuleDate);
            }

            /**
             * 排班周数规则
             */
            RuleResult<OnceScheduleWeeks> weekResult = ruleService.check(RuleConstant.ONCE_SCHEDULE_WEEKS, ruleParam);
            if (weekResult != null && weekResult.getCode() == 0 && weekResult.getResult()) {
                Integer weeks = weekResult.getData().getWeeks();
                paramJson.put(PARAM_SCHEDULE_WEEKS, weeks);
            }

            paramJson.put(PARAM_ORG_UUID, task.getOrgCode());
            paramJson.put(PARAM_ORG_DEPT_UUID, orgDeptUuid);
            paramJson.put(PARAM_DEPT_MAX_NUM,deptMaxNum);
            paramJson.put(PARAM_DEPT_START_NUM,orderNum);
            // 计算下一个科室的开始编码
            orderNum += deptMaxNum;
            array.add(paramJson);

        }
        return array;
    }

    /**
     * 1、获取
     *
     * @param task
     * @param paramJson
     * @return
     * @throws Exception
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public JSONObject taskRun(BaseOrgTask task, JSONObject paramJson) throws Exception {
        String orgUuid = paramJson.getString(PARAM_ORG_UUID);
        String orgDeptUuid = paramJson.getString(PARAM_ORG_DEPT_UUID);
        //String schdeuleDate = paramJson.getString(PARAM_SCHDEULE_DATE);
        Integer scheduleWeeks = paramJson.getInteger(PARAM_SCHEDULE_WEEKS);
        Integer deptStartNumOrgin = paramJson.getInteger(PARAM_DEPT_START_NUM);
        Integer deptMaxNum = deptStartNumOrgin + paramJson.getInteger(PARAM_DEPT_MAX_NUM);
        Map<Long, Integer> deptStartNumMap = new HashMap<>();


        /**
         * 获取此科室在库中的排班的最晚日期
         */
        Date lastScheduleDay = orderScheduleExtMapper.selectLastSdate(orgUuid, orgDeptUuid);

        /**
         * 开始排班日期
         */
        Date startDay;
        if (lastScheduleDay == null || lastScheduleDay.getTime() < System.currentTimeMillis()) {
            startDay = DateUtils.parseDate(DateFormatUtils.format(new Date(), "yyyy-MM-dd"),
                    "yyyy-MM-dd");
        } else {
            startDay = lastScheduleDay;
        }

        /**
         * 结束排班日期
         */
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        Integer dayNum = cal.get(Calendar.DAY_OF_WEEK);
        // 下一个星期天
        Date nextSunday = getNextWeek(new Date(), 7);
        // 需要排班到的日期
        Date endDay = DateUtils.addWeeks(nextSunday, scheduleWeeks);

        /**
         * 查询此科室的假日
         */
        BaseOrgCalendarExample calendarExample = new BaseOrgCalendarExample();
        BaseOrgCalendarExample.Criteria calendarExampleCriteria = calendarExample.createCriteria();
        calendarExampleCriteria.andHolidayDateBetween(startDay, endDay);
        calendarExampleCriteria.andOrgCodeEqualTo(orgUuid);
        calendarExampleCriteria.andDeptCodeEqualTo(orgDeptUuid);
        calendarExampleCriteria.andStatusEqualTo(DictPubStatus.NORMAL.key);
        List<BaseOrgCalendar> baseOrgCalendarList = baseOrgCalendarMapper.selectByExample(calendarExample);
        /**
         * 构建假日的map
         */
        Map<Long, Map<String, Object>> calendarMap = genarateCalendarMap(baseOrgCalendarList);

        /**
         * 查询排班停诊
         */
        List<StopReplaceApply> stopReplaceApplies = stopReplaceApplyExtMapper.selectStopByTime(orgUuid, orgDeptUuid, startDay, endDay);
        /**
         * 构建排班停诊map
         */
        Map<String, Set<String>> stopMap = genarateStopMap(stopReplaceApplies);


        Map<String, List<Date>> weekDays = getWeekDays(startDay, endDay);

        List<OrderSchedule> insertOSdata = new ArrayList<>();
        List<OrderSchedulePart> insertOSPdata = new ArrayList<>();
        List<OrderScheduleRespool> insertOSRdata = new ArrayList<>();

        // 查询排班模板表 order_schedule_tp
        List<OrderScheduleTpWithPart> orderScheduleTpWithPartList = orderScheduleTpExtMapper.selectScheduleWithPart(orgUuid, orgDeptUuid);

        // 生成scheduleList和schedulepartList
        for (OrderScheduleTpWithPart orderScheduleTpWithPart : orderScheduleTpWithPartList) {
            /**
             * 获取每个星期的日期数组
             */
            String weekNum = orderScheduleTpWithPart.getWeekInfo();
            List<Date> dateList = weekDays.get(weekNum);

            /**
             * 生成OrderSchedule
             */
            OrderSchedule orderSchedule = new OrderSchedule();
            BeanUtils.copyProperties(orderSchedule, orderScheduleTpWithPart);
            orderSchedule.setCreateTime(new Date());
            orderSchedule.setUpdateTime(new Date());
            orderSchedule.setBuildTime(new Date());
            orderSchedule.setCreateUserUuid(DEFAULT_USER_ID);
            orderSchedule.setBuildUserCode(DEFAULT_USER_ID);
            orderSchedule.setUpdateUserUuid(DEFAULT_USER_ID);
            orderSchedule.setOrderSource(DictOrderSource.AUTO_GENERATE.key);
            String timePartType = orderSchedule.getTimePartType();
            String orgEmpUuid = orderSchedule.getOrgEmpCode();

            /**
             * 获取此医生的停诊信息
             */
            Set<String> stopSet = stopMap.get(orderScheduleTpWithPart.getOrgDeptEmpCode());

            for (Date date : dateList) {
                /**
                 * 此日期的号源排列号
                 */
                Integer deptStartNum = deptStartNumMap.get(date.getTime());
                if(deptStartNum == null){
                    deptStartNum = deptStartNum = paramJson.getInteger(PARAM_DEPT_START_NUM);
                }

                /**
                 * 生成当日的orderSchedule
                 */
                Date sdate1 = date;
                List<Date> sdataList = new ArrayList<>();
                sdataList.add(sdate1);

                Map<String, Object> bocMap = calendarMap.get(date.getTime());

                if(bocMap != null){
                    if(DicPubYesno.YES.key.equals(bocMap.get(PARAM_CALENDAR_IS_EXCHANGE))){
                        sdate1 = (Date) bocMap.get(PARAM_CALENDAR_DAY);
                    }else if("h".equals(bocMap.get(PARAM_CALENDAR_WORK_OR_HOLIDAY))){
                        continue;
                    }else if("w".equals(bocMap.get(PARAM_CALENDAR_WORK_OR_HOLIDAY))){
                        Date sdate2 = (Date) bocMap.get(PARAM_CALENDAR_DAY);
                        sdataList.add(sdate2);
                    }
                }
                for(Date sdate : sdataList) {
                    OrderSchedule orderScheduleForDate = new OrderSchedule();
                    BeanUtils.copyProperties(orderScheduleForDate, orderSchedule);
                    orderScheduleForDate.setSdate(sdate);
                    orderScheduleForDate.setWeekInfo(getWeekDayByDate(sdate).toString());
                    String osUuid = genarateId();
                    orderScheduleForDate.setOsUuid(osUuid);
                    orderScheduleForDate.setStatus(DictHospScheduleStatus.APPROVING.key);

                    //boolean isAddSchedule = false;

                    // 判断是否在停诊时间段
                    if (stopSet != null) {
                        String stopKey = String.format(STOP_TIME_KEY, DateFormatUtils.format(date, "yyyy-MM-dd"), orderSchedule.getTimePartType());
                        if (stopSet.contains(stopKey)) {
                            continue;
                        }
                    }
                    insertOSdata.add(orderScheduleForDate);

                    orderScheduleTpWithPart.getOrderSchedulePartTpList().sort((t1, t2) -> {
                        if (compareTimeBegin(t1.getTimeBegin(), t2.getTimeBegin())) {
                            return 1;
                        } else {
                            return -1;
                        }
                    });

                    String configQueueNo = BaseConfigUtils.getParam(CONFIG_CURRENT_QUEUE_NO);

                    Long yuyueStart;
                    Long todayStart;

                    if (StringUtils.isBlank(configQueueNo) || "2".equals(configQueueNo)) {
                        // 当日号排序开始
                        todayStart = orderScheduleTpWithPart.getOrderSchedulePartTpList().stream()
                                .mapToInt((s) -> s.getAppoTotal()).summaryStatistics().getSum() + 1;
                        // 预约号排序开始
                        yuyueStart = 1L;
                    } else {
                        // 当日号排序开始
                        yuyueStart = orderScheduleTpWithPart.getOrderSchedulePartTpList().stream()
                                .mapToInt((s) -> s.getCurrentTotal()).summaryStatistics().getSum() + 1;
                        // 预约号排序开始
                        todayStart = 1L;
                    }

                    // 生成时间段号源
                    for (OrderSchedulePartTp orderSchedulePartTp : orderScheduleTpWithPart.getOrderSchedulePartTpList()) {

                        // 生成orderSchedulePart
                        OrderSchedulePart orderSchedulePart = new OrderSchedulePart();
                        BeanUtils.copyProperties(orderSchedulePart, orderSchedulePartTp);
                        orderSchedulePart.setOrgCode(orgUuid);
                        orderSchedulePart.setOrgDeptCode(orgDeptUuid);
                        orderSchedulePart.setOrgEmpCode(orgEmpUuid);
                        orderSchedulePart.setCreateTime(new Date());
                        orderSchedulePart.setUpdateTime(new Date());
                        orderSchedulePart.setCreateUserUuid(DEFAULT_USER_ID);
                        orderSchedulePart.setUpdateUserUuid(DEFAULT_USER_ID);
                        orderSchedulePart.setSdate(sdate);
                        orderSchedulePart.setTimePartType(timePartType);
                        orderSchedulePart.setOrderSource(DictOrderSource.AUTO_GENERATE.key);
                        orderSchedulePart.setScheduleUuid(osUuid);
                        String ospUuid = genarateId();
                        orderSchedulePart.setOspUuid(ospUuid);
                        orderSchedulePart.setStatus(DictHospScheduleStatus.APPROVING.key);

                        insertOSPdata.add(orderSchedulePart);

                        //isAddSchedule = true;
                        // 生成ORDER_SCHEDULE_RESPOOL
                        //当日号
                        Integer currentTotal = orderSchedulePart.getCurrentTotal();
                        for (int i = 1; i <= currentTotal; i++) {
                            if (deptStartNum.intValue() == deptMaxNum.intValue()) {
                                continue;
                            }
                            // 生成当日号号源
                            OrderScheduleRespool orderScheduleRespool = genarateRespool(orderSchedulePart, deptStartNum,
                                    DictHospOrderResType.SCENE.key, todayStart, DictHospScheduleStatus.APPROVING.key);
                            deptStartNum++;
                            todayStart++;
                            insertOSRdata.add(orderScheduleRespool);
                        }
                        // 预约号
                        Integer appoTotal = orderSchedulePart.getAppoTotal();
                        for (int i = 1; i <= appoTotal; i++) {
                            if (deptStartNum.intValue() == deptMaxNum.intValue()) {
                                continue;
                            }
                            // 生成预约号号源
                            OrderScheduleRespool orderScheduleRespool = genarateRespool(orderSchedulePart, deptStartNum,
                                    DictHospOrderResType.ONLINE.key, yuyueStart, DictHospScheduleStatus.APPROVING.key);
                            deptStartNum++;
                            yuyueStart++;
                            insertOSRdata.add(orderScheduleRespool);
                        }
                    }


                    deptStartNumMap.put(date.getTime(), deptStartNum);
                }
            }
        }
        /**
         * 批量插入数据库
         */
        try {
            orderScheduleExtMapper.insertOrderSchedules(insertOSdata);
            orderSchedulePartExtMapper.insertOrderScheduleParts(insertOSPdata);
            orderScheduleRespoolExtMapper.insertOrderScheduleRespools(insertOSRdata);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("insert orderSchedule,orderSchedulePart,orderScheduleRespool has error",e);
            removeSchedule(orgUuid,orgDeptUuid,startDay,endDay);
        }
        return null;
    }


    @Override
    public JSONObject resultDeal() {
        return null;
    }

    /**
     * 删除指定时间段的排班
     *
     * @param orgCode
     * @param orgDeptCode
     * @param startDate
     * @param endDate
     * @return
     */
    private boolean removeSchedule(String orgCode,String orgDeptCode,Date startDate,Date endDate){
        try {
            orderScheduleExtMapper.deleteOrderScheduleWithDocAndDate(orgCode,orgDeptCode,startDate,endDate);
            orderSchedulePartExtMapper.deleteOrderSchedulePartsWithDocAndDate(orgCode,orgDeptCode,startDate,endDate);
            orderScheduleRespoolExtMapper.deleteOrderScheduleRespoolsWithDeptAndDate(orgCode,orgDeptCode,startDate,endDate);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("delete orderSchedule,orderSchedulePart,orderScheduleRespool has error",e);
            return false;
        }
    }

    /**
     * 比较两个 HH:mm 格式的日期
     *
     * @param t1
     * @param t2
     * @return
     */
    protected boolean compareTimeBegin(String t1,String t2){
        Date t1Date = null;
        Date t2Date = null;
        try {
            t1Date = DateUtils.parseDate(t1,"HH:mm");
            t2Date = DateUtils.parseDate(t2,"HH:mm");
        } catch (ParseException e) {
            log.error("转换日期错误",e);
            e.printStackTrace();
            return false;
        }
        if(t1Date.getTime() >= t2Date.getTime()) {
            return true;
        }else{
            return false;
        }
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

    /**
     * 按周来分组日期
     *
     * @param startDay
     * @param endDay
     * @return
     */
    private Map<String, List<Date>> getWeekDays(Date startDay, Date endDay) {
        Map<String, List<Date>> map = new HashMap<>();
        Date temp = startDay;

        while (temp.getTime() <= endDay.getTime()) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(temp);
            Integer dayNum = cal.get(Calendar.DAY_OF_WEEK);
            if (dayNum == 1) {
                dayNum = 7;
            } else {
                dayNum = dayNum - 1;
            }
            List<Date> dates = map.get(dayNum.toString());
            if (dates == null) {
                dates = new ArrayList<>();
                map.put(dayNum.toString(), dates);
            }
            dates.add(new Date(temp.getTime()));
            temp = DateUtils.addDays(temp, 1);
        }

        return map;
    }

    /**
     * 日期转换成星期
     *
     * @param date
     * @return
     */
    private Integer getWeekDayByDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        Integer dayNum = cal.get(Calendar.DAY_OF_WEEK);
        if (dayNum == 1) {
            dayNum = 7;
        } else {
            dayNum = dayNum - 1;
        }
        return dayNum;
    }

    /**
     * 生成ID
     *
     * @return
     */
    private String genarateId() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    /**
     * 生成订单号
     *
     * @param date
     * @param num
     * @return
     */
    private String genarateSerial(Date date, Integer num) {

        String dateStr = DateFormatUtils.format(date, "yyMMdd");
        String numStr = String.format("%05d", num);

        return dateStr + numStr;

    }

    /**
     * 生成OrderScheduleRespool
     *
     * @param orderSchedulePart
     * @param index
     * @return
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    protected OrderScheduleRespool genarateRespool(OrderSchedulePart orderSchedulePart,
                                                 int index, String orderResType,Long queueNum,String status) throws InvocationTargetException, IllegalAccessException {
        OrderScheduleRespool orderScheduleRespool = new OrderScheduleRespool();
        BeanUtils.copyProperties(orderScheduleRespool, orderSchedulePart);
        orderScheduleRespool.setOrderResType(orderResType);
        orderScheduleRespool.setQueueNo(queueNum.intValue());
        orderScheduleRespool.setOsrUuid(genarateId());
        orderScheduleRespool.setOsrSerial(genarateSerial(orderSchedulePart.getSdate(), index));
        orderScheduleRespool.setScheduleUuid(orderSchedulePart.getScheduleUuid());
        orderScheduleRespool.setSchedulePartUuid(orderSchedulePart.getOspUuid());
        orderScheduleRespool.setStatus(status);
        return orderScheduleRespool;
    }

    /**
     * 构建假日的时间Map
     *
     * @param baseOrgCalendarList
     * @return
     */
    private Map<Long, Map<String,Object>> genarateCalendarMap(List<BaseOrgCalendar> baseOrgCalendarList){
        Map<Long, Map<String,Object>> calendarMap = new HashMap<>();
        for (BaseOrgCalendar calendar : baseOrgCalendarList) {
            String isExchange = calendar.getIsExchange();
            Map<String,Object> workMap = new HashMap<>();
            Map<String,Object> holidayMap = new HashMap<>();
            if (DicPubYesno.YES.key.equals(isExchange)) {
                workMap.put(PARAM_CALENDAR_IS_EXCHANGE,isExchange);
                holidayMap.put(PARAM_CALENDAR_IS_EXCHANGE,isExchange);
                workMap.put(PARAM_CALENDAR_DAY,calendar.getHolidayDate());
                holidayMap.put(PARAM_CALENDAR_DAY,calendar.getWorkDate());
                calendarMap.put(calendar.getWorkDate().getTime(), workMap);
                calendarMap.put(calendar.getHolidayDate().getTime(), holidayMap);
            }else{
                workMap.put(PARAM_CALENDAR_IS_EXCHANGE,isExchange);
                holidayMap.put(PARAM_CALENDAR_IS_EXCHANGE,isExchange);
                workMap.put(PARAM_CALENDAR_WORK_OR_HOLIDAY,"w");
                holidayMap.put(PARAM_CALENDAR_WORK_OR_HOLIDAY,"h");
                workMap.put(PARAM_CALENDAR_DAY,calendar.getHolidayDate());
                calendarMap.put(calendar.getWorkDate().getTime(), workMap);
                calendarMap.put(calendar.getHolidayDate().getTime(), holidayMap);
            }
        }
        return calendarMap;
    }

    /**
     * 构建停诊的时间Map
     *
     * @param stopReplaceApplies
     * @return
     */
    private Map<String, Set<String>> genarateStopMap(List<StopReplaceApply> stopReplaceApplies) {
        Map<String, Set<String>> stopMap = new HashMap<>();
        for(StopReplaceApply stop : stopReplaceApplies){
            Set<String> stopSet = stopMap.get(stop.getDocCode());
            if(stopSet == null){
                stopSet = new HashSet<>();
                stopMap.put(stop.getDocCode(),stopSet);
            }
            boolean isSameDay = DateUtils.isSameDay(stop.getStartDate(),stop.getStopDate());

            for(Date temp = new Date(stop.getStartDate().getTime()); temp.getTime() <= stop.getStopDate().getTime(); temp = DateUtils.addDays(temp,1)){
                Integer startTime = Integer.parseInt(DictHospTimePartType.MORNING.key);
                Integer endTime = Integer.parseInt(DictHospTimePartType.EVENING.key);
                if(isSameDay){
                    startTime = Integer.parseInt(stop.getStartTime());
                    endTime = Integer.parseInt(stop.getStopTime());
                }else if(DateUtils.isSameDay(stop.getStartDate(),temp)){
                    startTime = Integer.parseInt(stop.getStartTime());
                }else if(DateUtils.isSameDay(stop.getStopDate(),temp)){
                    endTime = Integer.parseInt(stop.getStopTime());
                }

                for(;startTime <= endTime; startTime++){
                    String stopKey = String.format(STOP_TIME_KEY,DateFormatUtils.format(temp,"yyyy-MM-dd"),startTime.toString());
                    stopSet.add(stopKey);
                }

            }
        }
        return stopMap;
    }

}
