package com.imooc.controller.center;
/**
 * @author hzc
 * @date 2020-07-12 11:16
 */

import com.imooc.bo.center.OrderItemsCommentBO;
import com.imooc.controller.BaseController;
import com.imooc.enums.YesOrNo;
import com.imooc.pojo.OrderItems;
import com.imooc.pojo.Orders;
import com.imooc.service.center.MyCommentService;
import com.imooc.service.center.MyOrderService;
import com.imooc.utils.JSONResult;
import com.imooc.utils.PagedGridResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author hzc
 * @date 2020-07-12 11:16
 */
@Api("用户中心-评价相关接口")
@RestController
@RequestMapping("mycomments")
public class MyCommentsController extends BaseController {

    @Autowired
    private MyOrderService myOrderService;

    @Autowired
    private MyCommentService myCommentService;

    @ApiOperation("查询需要评价的商品")
    @PostMapping("pending")
    public JSONResult pending(@RequestParam String userId, @RequestParam String orderId) {

        JSONResult checkResult = checkOrder(orderId, userId);
        if (checkResult.getStatus() != 200) {
            return JSONResult.errorMsg("无订单");
        }

        List<OrderItems> orderItems = myCommentService.queryPendingComment(orderId);

        return JSONResult.ok(orderItems);
    }

    @ApiOperation("保存评论")
    @PostMapping("saveList")
    public JSONResult saveList(@RequestParam String userId,
                               @RequestParam String orderId,
                               @RequestBody List<OrderItemsCommentBO> orderItemList) {
        JSONResult checkResult = checkOrder(orderId, userId);
        if (checkResult.getStatus() != 200) {
            return JSONResult.errorMsg("无订单");
        }
        Orders orders = (Orders) checkResult.getData();
        if (orders.getIsComment() == YesOrNo.YES.type) {
            return JSONResult.errorMsg("订单已评价");
        }
        myCommentService.saveComments(orderId, userId, orderItemList);
        return JSONResult.ok();
    }

    @ApiOperation("查询我的评价")
    @PostMapping("query")
    public JSONResult query(@RequestParam String userId,
                            @ApiParam(name = "page", value = "页数", required = false)
                                 @RequestParam Integer page,
                            @ApiParam(name = "pageSize", value = "每页数量", required = false)
                                 @RequestParam Integer pageSize) {
        if (page == null) {
            page = 0;
        }
        if (pageSize == null) {
            pageSize = PAGE_SIZE;
        }
        PagedGridResult pagedGridResult = myCommentService.queryMyComments(userId, page, pageSize);
        return JSONResult.ok(pagedGridResult);
    }


    private JSONResult checkOrder(String orderId, String userId) {
        Orders orders = myOrderService.queryMyOrder(orderId, userId);

        if (orders == null) {
            return JSONResult.errorMsg("");
        }
        return JSONResult.ok(orders);

    }

}
