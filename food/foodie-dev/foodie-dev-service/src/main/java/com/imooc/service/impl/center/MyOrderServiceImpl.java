package com.imooc.service.impl.center;
/**
 * @author hzc
 * @date 2020-07-12 00:00
 */

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.imooc.enums.OrderStatusEnum;
import com.imooc.enums.YesOrNo;
import com.imooc.mapper.OrderStatusMapper;
import com.imooc.mapper.OrdersMapper;
import com.imooc.mapper.OrdersMapperCustom;
import com.imooc.pojo.OrderStatus;
import com.imooc.pojo.Orders;
import com.imooc.service.center.MyOrderService;
import com.imooc.utils.PagedGridResult;
import com.imooc.vo.MyOrdersVO;
import com.imooc.vo.OrderStatusCountsVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author hzc
 * @date 2020-07-12 00:00
 */
@Slf4j
@Service
public class MyOrderServiceImpl implements MyOrderService {

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private OrdersMapperCustom ordersMapperCustom;

    @Autowired
    private OrderStatusMapper orderStatusMapper;

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public PagedGridResult queryMyOrders(String userId, Integer orderStatus, Integer page, Integer pageSize) {
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("userId",userId);
        if(orderStatus != null){
            paramMap.put("orderStatus",orderStatus);
        }

        PageHelper.startPage(page,pageSize);

        List<MyOrdersVO> myOrders = ordersMapperCustom.queryMyOrders(paramMap);

        return setterPagedGrid(myOrders,page);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public boolean updateDeliverOrderStatus(String orderId) {
        OrderStatus orderStatus = new OrderStatus();
        orderStatus.setOrderStatus(OrderStatusEnum.WAIT_RECEIVE.type);
        orderStatus.setOrderId(orderId);
        orderStatus.setDeliverTime(new Date());

        int updateRows = orderStatusMapper.updateByPrimaryKeySelective(orderStatus);
        if(updateRows < 1) {
            log.error("【{}】订单出货失败!",orderId);
            return false;
        }
        return true;
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public Orders queryMyOrder(String orderId, String userId) {
        Example example = new Example(Orders.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("id",orderId);
        criteria.andEqualTo("userId",userId);
        Orders orders = ordersMapper.selectOneByExample(example);
        return orders;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public boolean updateReceiveOrderStatus(String orderId) {
        OrderStatus orderStatus = new OrderStatus();
        orderStatus.setOrderStatus(OrderStatusEnum.SUCCESS.type);
        orderStatus.setOrderId(orderId);
        orderStatus.setSuccessTime(new Date());

        int updateRows = orderStatusMapper.updateByPrimaryKeySelective(orderStatus);
        if(updateRows < 1) {
            log.error("【{}】订单确认收货失败!",orderId);
            return false;
        }
        return true;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public boolean deleteOrder(String orderId) {
        Orders orders = new Orders();
        orders.setId(orderId);
        orders.setIsDelete(YesOrNo.YES.type);
        orders.setUpdatedTime(new Date());

        int updateRows = ordersMapper.updateByPrimaryKeySelective(orders);
        if(updateRows < 1) {
            log.error("【{}】订单删除失败!",orderId);
            return false;
        }
        return true;
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public OrderStatusCountsVO getOrderStatusCounts(String userId) {

        Map<String,Object> paramMap = new HashMap<>();

        paramMap.put("userId",userId);

        paramMap.put("orderStatus",OrderStatusEnum.WAIT_PAY.type);
        Integer waitPayCount = ordersMapperCustom.getOrderStatusCount(paramMap);

        paramMap.put("orderStatus",OrderStatusEnum.WAIT_DELIVER.type);
        Integer waitDeliverCount = ordersMapperCustom.getOrderStatusCount(paramMap);

        paramMap.put("orderStatus",OrderStatusEnum.WAIT_RECEIVE.type);
        Integer waitReceiveCount = ordersMapperCustom.getOrderStatusCount(paramMap);

        paramMap.put("orderStatus",OrderStatusEnum.SUCCESS.type);
        paramMap.put("isComment",YesOrNo.NO.type);
        Integer waitCommentCount = ordersMapperCustom.getOrderStatusCount(paramMap);

        OrderStatusCountsVO result = new OrderStatusCountsVO(waitPayCount,waitDeliverCount,waitReceiveCount,waitCommentCount);

        return result;
    }

    @Override
    public PagedGridResult getOrdersTrend(String userId, Integer page, Integer pageSize) {

        PageHelper.startPage(page,pageSize);

        List<OrderStatus> myOrderTrend = ordersMapperCustom.getMyOrderTrend(userId);
        return setterPagedGrid(myOrderTrend,page);
    }

    private PagedGridResult setterPagedGrid(List<?> list, Integer page) {
        PageInfo<?> pageList = new PageInfo<>(list);
        PagedGridResult grid = new PagedGridResult();
        grid.setPage(page);
        grid.setRows(list);
        grid.setTotal(pageList.getPages());
        grid.setRecords(pageList.getTotal());
        return grid;
    }
}
