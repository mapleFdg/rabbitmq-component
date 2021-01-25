package com.imooc.service.impl.center;
/**
 * @author hzc
 * @date 2020-07-12 11:21
 */

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.imooc.bo.center.OrderItemsCommentBO;
import com.imooc.enums.YesOrNo;
import com.imooc.mapper.ItemsCommentsMapperCustom;
import com.imooc.mapper.OrderItemsMapper;
import com.imooc.mapper.OrderStatusMapper;
import com.imooc.mapper.OrdersMapper;
import com.imooc.pojo.OrderItems;
import com.imooc.pojo.OrderStatus;
import com.imooc.pojo.Orders;
import com.imooc.service.center.MyCommentService;
import com.imooc.utils.PagedGridResult;
import com.imooc.vo.MyCommentVO;
import lombok.extern.slf4j.Slf4j;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author hzc
 * @date 2020-07-12 11:21
 */
@Slf4j
@Service
public class MyCommentServiceImpl implements MyCommentService {

    @Autowired
    private OrderItemsMapper orderItemsMapper;

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private OrderStatusMapper orderStatusMapper;

    @Autowired
    private ItemsCommentsMapperCustom itemsCommentsMapperCustom;

    @Autowired
    private Sid sid;

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public List<OrderItems> queryPendingComment(String orderId) {
        OrderItems query = new OrderItems();
        query.setOrderId(orderId);
        return orderItemsMapper.select(query);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void saveComments(String orderId, String userId, List<OrderItemsCommentBO> orderItemsComments) {

        // 保存评论信息
        for(OrderItemsCommentBO oi : orderItemsComments){
             oi.setCommentId(sid.nextShort());
        }
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("userId",userId);
        paramMap.put("orderItemsComments",orderItemsComments);

        itemsCommentsMapperCustom.saveComments(paramMap);

        // 修改订单评论的状态
        Orders updateOrders = new Orders();
        updateOrders.setId(orderId);
        updateOrders.setUserId(userId);
        updateOrders.setIsComment(YesOrNo.YES.type);
        updateOrders.setUpdatedTime(new Date());
        ordersMapper.updateByPrimaryKeySelective(updateOrders);

        // 修改评论时间
        OrderStatus updateStatus = new OrderStatus();
        updateStatus.setOrderId(orderId);
        updateStatus.setCommentTime(new Date());
        orderStatusMapper.updateByPrimaryKeySelective(updateStatus);
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public PagedGridResult queryMyComments(String userId, Integer page, Integer pageSize) {
        PageHelper.startPage(page,pageSize);
        List<MyCommentVO> myCommentVOS = itemsCommentsMapperCustom.queryMyComments(userId);
        return setterPagedGrid(myCommentVOS,page);
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
