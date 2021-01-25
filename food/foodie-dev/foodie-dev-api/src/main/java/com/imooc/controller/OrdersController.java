package com.imooc.controller;
/**
 * @author hzc
 * @date 2020-07-05 10:04
 */

import com.imooc.bo.ShopcartBO;
import com.imooc.bo.SubmitOrderBO;
import com.imooc.enums.OrderStatusEnum;
import com.imooc.pojo.OrderStatus;
import com.imooc.service.OrderService;
import com.imooc.utils.CookieUtils;
import com.imooc.utils.JSONResult;
import com.imooc.utils.JsonUtils;
import com.imooc.utils.RedisOperator;
import com.imooc.vo.MerchantOrdersVO;
import com.imooc.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * @author hzc
 * @date 2020-07-05 10:04
 */
@Slf4j
@Api(value = "订单API接口", tags = {"订单API接口"})
@RestController
@RequestMapping("orders")
public class OrdersController extends BaseController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private RedisOperator redisOperator;

    @ApiOperation(value = "生成订单", notes = "生成订单")
    @PostMapping("/create")
    public JSONResult create(@RequestBody @Valid SubmitOrderBO submitOrderBO,
                             BindingResult bindingResult, HttpServletRequest request, HttpServletResponse response) {

        if (bindingResult.hasErrors()) {
            Map<String, String> errorMap = getErrors(bindingResult);
            log.error("create order error : {}", errorMap);
            return JSONResult.errorMap(errorMap);
        }

        /**
         * 1.创建订单
         * 2.创建订单以后，移除购物城中已结算的商品
         * 3.向支付中心发送当前订单，用于保存支付中心的订单数据
         *
         */

        String shopCatJson = redisOperator.get(SHOP_CART + ":" + submitOrderBO.getUserId());

        if(StringUtils.isBlank(shopCatJson)){
            return JSONResult.errorMsg("购物车为空！");
        }

        List<ShopcartBO> shopcartList = JsonUtils.jsonToList(shopCatJson,ShopcartBO.class);


        /**
         * 1.创建订单
         */
        OrderVO orderVo = orderService.createOrder(shopcartList,submitOrderBO);

        /**
         * 2.移除购物车的已结算商品
         */
        List<ShopcartBO> toBeRemovedShopcartList = orderVo.getToBeRemovedShopcartList();
        shopcartList.removeAll(toBeRemovedShopcartList);
        redisOperator.set(SHOP_CART + ":" + submitOrderBO.getUserId(), JsonUtils.objectToJson(shopcartList));
        // 同步到cookies
        CookieUtils.setCookie(request,response,SHOP_CART,JsonUtils.objectToJson(shopcartList),true);

        /**
         * 向支付中心创建支付订单
         */
        MerchantOrdersVO merchantOrdersVO = orderVo.getMerchantOrdersVO();
        merchantOrdersVO.setReturnUrl(PAY_RETURN_URL);

        // 为了方便测试，统一设置为1分钱
        merchantOrdersVO.setAmount(1);

        HttpHeaders headers = new HttpHeaders();
        headers.set("imoocUserId", PAY_USER);
        headers.set("password", PAY_PASSWORD);
        HttpEntity<MerchantOrdersVO> entity = new HttpEntity<>(merchantOrdersVO, headers);
        ResponseEntity<JSONResult> payResponse = restTemplate.postForEntity(paymentUrl, entity, JSONResult.class);
        JSONResult responseResult = payResponse.getBody();
        if (responseResult.getStatus() != HttpStatus.OK.value()) {
            return JSONResult.errorMsg("支付中心创建订单失败");
        }
        return JSONResult.ok(orderVo.getOrderId());
    }

    /**
     * 接收支付平台支付成功通知
     *
     * @param merchantOrderId
     * @return
     */
    @ApiIgnore
    @PostMapping("/notifyMerchantOrderPaid")
    public Integer notifyMerchantOrderPaid(String merchantOrderId) {
        log.info("接收到支付成功通知!{}",merchantOrderId);
        orderService.updateOrderStatus(merchantOrderId, OrderStatusEnum.WAIT_DELIVER.type);
        return HttpStatus.OK.value();
    }

    @ApiOperation(value = "查询订单支付状态", notes = "查询订单支付状态")
    @PostMapping("/getPaidOrderInfo")
    public JSONResult getPaidOrderInfo(
            @ApiParam(name = "orderId", value = "订单ID", required = true)
            @RequestParam String orderId) {
        OrderStatus orderStatus = orderService.queryOrderStatusInfo(orderId);

        return JSONResult.ok(orderStatus);


    }


}
