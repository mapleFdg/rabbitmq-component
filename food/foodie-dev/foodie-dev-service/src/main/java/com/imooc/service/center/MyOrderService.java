package com.imooc.service.center;

import com.imooc.pojo.Orders;
import com.imooc.utils.PagedGridResult;
import com.imooc.vo.OrderStatusCountsVO;

/**
 * @author hzc
 * @date 2020-07-12 00:00
 */
public interface MyOrderService {

    public PagedGridResult queryMyOrders(String userId, Integer orderStatus, Integer page, Integer pageSize);

    public boolean updateDeliverOrderStatus(String orderId);

    public Orders queryMyOrder(String orderId, String userId);

    public boolean updateReceiveOrderStatus(String orderId);

    public boolean deleteOrder(String orderId);

    public OrderStatusCountsVO getOrderStatusCounts(String userId);

    public PagedGridResult getOrdersTrend(String userId, Integer page, Integer pageSize);
}
