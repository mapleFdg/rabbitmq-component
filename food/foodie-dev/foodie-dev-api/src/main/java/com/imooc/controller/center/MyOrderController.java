package com.imooc.controller.center;
/**
 * @author hzc
 * @date 2020-07-12 00:07
 */

import com.imooc.controller.BaseController;
import com.imooc.pojo.Orders;
import com.imooc.service.center.MyOrderService;
import com.imooc.utils.JSONResult;
import com.imooc.utils.PagedGridResult;
import com.imooc.vo.OrderStatusCountsVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author hzc
 * @date 2020-07-12 00:07
 */
@Api(value = "用户中心我的订单API接口", tags = {"用户中心我的订单API接口"})
@RestController
@RequestMapping("myorders")
public class MyOrderController extends BaseController {

    @Autowired
    private MyOrderService myOrderService;

    @ApiOperation(value = "查询我的订单", notes = "查询我的订单")
    @PostMapping("query")
    public JSONResult query(
            @ApiParam(name = "userId", value = "用户ID", required = true)
            @RequestParam String userId,
            @ApiParam(name = "orderStatus", value = "订单状态", required = false)
            @RequestParam Integer orderStatus,
            @ApiParam(name = "page", value = "页数", required = false)
            @RequestParam Integer page,
            @ApiParam(name = "pageSize", value = "每页数量", required = false)
            @RequestParam Integer pageSize) {
        if (StringUtils.isBlank(userId)) {
            return JSONResult.errorMsg("");
        }
        if (page == null) {
            page = 0;
        }
        if (pageSize == null) {
            pageSize = PAGE_SIZE;
        }
        PagedGridResult pagedGridResult = myOrderService.queryMyOrders(userId, orderStatus, page, pageSize);
        return JSONResult.ok(pagedGridResult);
    }

    @ApiOperation(value = "商品发货")
    @PostMapping("deliver")
    public JSONResult deliver(
            @ApiParam(name = "orderId", value = "订单ID", required = true)
            @RequestParam String orderId) {
        if (StringUtils.isBlank(orderId)) {
            return JSONResult.errorMsg("");
        }
        if (!myOrderService.updateDeliverOrderStatus(orderId)) {
            return JSONResult.errorMsg("订单发货失败");
        }
        return JSONResult.ok();
    }

    @ApiOperation(value = "订单确认收货", notes = "订单确认收货")
    @PostMapping("confirmReceive")
    public JSONResult confirmReceive(
            @ApiParam(name = "orderId", value = "订单ID", required = true)
            @RequestParam String orderId,
            @ApiParam(name = "userId", value = "用户ID", required = true)
            @RequestParam String userId) {
        if (StringUtils.isBlank(orderId)) {
            return JSONResult.errorMsg("");
        }
        if (StringUtils.isBlank(userId)) {
            return JSONResult.errorMsg("");
        }

        JSONResult checkResult = checkOrder(orderId, userId);
        if (checkResult.getStatus() != 200) {
            return JSONResult.errorMsg("订单信息不正确");
        }

        if (!myOrderService.updateReceiveOrderStatus(orderId)) {
            return JSONResult.errorMsg("订单确认收货失败");
        }
        return JSONResult.ok();
    }

    @ApiOperation(value = "删除订单", notes = "删除订单")
    @PostMapping("delete")
    public JSONResult delete(
            @ApiParam(name = "orderId", value = "订单ID", required = true)
            @RequestParam String orderId,
            @ApiParam(name = "userId", value = "用户ID", required = true)
            @RequestParam String userId) {

        if (StringUtils.isBlank(orderId)) {
            return JSONResult.errorMsg("");
        }
        if (StringUtils.isBlank(userId)) {
            return JSONResult.errorMsg("");
        }
        JSONResult checkResult = checkOrder(orderId, userId);
        if (checkResult.getStatus() != 200) {
            return JSONResult.errorMsg("订单信息不正确");
        }

        if (!myOrderService.deleteOrder(orderId)) {
            return JSONResult.errorMsg("订单删除失败");
        }
        return JSONResult.ok();
    }

    @ApiOperation(value = "查询订单状态数", notes = "查询订单状态数")
    @PostMapping("statusCounts")
    public JSONResult statusCounts(
            @ApiParam(name = "userId", value = "用户ID", required = true)
            @RequestParam String userId) {
        if (StringUtils.isBlank(userId)) {
            return JSONResult.errorMsg("");
        }
        OrderStatusCountsVO orderStatusCounts = myOrderService.getOrderStatusCounts(userId);
        return JSONResult.ok(orderStatusCounts);
    }


    @ApiOperation(value = "查询订单动向", notes = "查询订单动向")
    @PostMapping("trend")
    public JSONResult trend(
            @ApiParam(name = "userId", value = "用户ID", required = true)
            @RequestParam String userId,
            @ApiParam(name = "page", value = "页数", required = false)
            @RequestParam Integer page,
            @ApiParam(name = "pageSize", value = "每页数量", required = false)
            @RequestParam Integer pageSize) {
        if (StringUtils.isBlank(userId)) {
            return JSONResult.errorMsg("");
        }
        if (page == null) {
            page = 0;
        }
        if (pageSize == null) {
            pageSize = PAGE_SIZE;
        }
        PagedGridResult ordersTrend = myOrderService.getOrdersTrend(userId, page, pageSize);
        return JSONResult.ok(ordersTrend);
    }

    private JSONResult checkOrder(String orderId, String userId) {
        Orders orders = myOrderService.queryMyOrder(orderId, userId);

        if (orders == null) {
            return JSONResult.errorMsg("");
        }
        return JSONResult.ok(orders);

    }

}
