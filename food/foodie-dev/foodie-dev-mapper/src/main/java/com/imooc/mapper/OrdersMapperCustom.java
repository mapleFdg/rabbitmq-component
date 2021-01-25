package com.imooc.mapper;

import com.imooc.pojo.OrderStatus;
import com.imooc.vo.MyOrdersVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface OrdersMapperCustom{

    public List<MyOrdersVO> queryMyOrders(@Param("paramMap") Map<String,Object> map);

    public Integer getOrderStatusCount(@Param("paramMap") Map<String,Object> map);

    public List<OrderStatus> getMyOrderTrend(String userId);
}