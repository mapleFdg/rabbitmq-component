package com.imooc.service;

import com.imooc.bo.ShopcartBO;
import com.imooc.bo.SubmitOrderBO;
import com.imooc.pojo.OrderStatus;
import com.imooc.vo.OrderVO;

import java.util.List;

/**
 * @author hzc
 * @date 2020-07-05 10:11
 */
public interface OrderService {

    public OrderVO createOrder(List<ShopcartBO> shopcartList, SubmitOrderBO submitOrderBO);

    public void updateOrderStatus(String orderId,Integer orderStatus);

    public OrderStatus queryOrderStatusInfo(String orderId);

    public void closeOrder();

}
