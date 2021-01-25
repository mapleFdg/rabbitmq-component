package com.imooc.service.center;

import com.imooc.bo.center.OrderItemsCommentBO;
import com.imooc.pojo.OrderItems;
import com.imooc.utils.PagedGridResult;

import java.util.List;

/**
 * @author hzc
 * @date 2020-07-12 11:20
 */
public interface MyCommentService {

    public List<OrderItems> queryPendingComment(String orderId);

    public void saveComments(String orderId, String userId, List<OrderItemsCommentBO> orderItemsComments);

    public PagedGridResult queryMyComments(String userId, Integer page, Integer pageSize);

}
