package com.imooc.service.impl;
/**
 * @author hzc
 * @date 2020-07-05 10:11
 */

import com.imooc.bo.ShopcartBO;
import com.imooc.bo.SubmitOrderBO;
import com.imooc.enums.OrderStatusEnum;
import com.imooc.enums.YesOrNo;
import com.imooc.mapper.OrderItemsMapper;
import com.imooc.mapper.OrderStatusMapper;
import com.imooc.mapper.OrdersMapper;
import com.imooc.pojo.*;
import com.imooc.service.AddressService;
import com.imooc.service.ItemService;
import com.imooc.service.OrderService;
import com.imooc.utils.DateUtil;
import com.imooc.vo.MerchantOrdersVO;
import com.imooc.vo.OrderVO;
import lombok.extern.slf4j.Slf4j;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author hzc
 * @date 2020-07-05 10:11
 */
@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    private Sid sid;

    @Autowired
    private AddressService addressService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private OrderItemsMapper orderItemsMapper;

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private OrderStatusMapper orderStatusMapper;

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public OrderVO createOrder(List<ShopcartBO> shopcartList, SubmitOrderBO submitOrderBO) {

        String userId = submitOrderBO.getUserId();
        String itemSpecIds = submitOrderBO.getItemSpecIds();
        String addressId = submitOrderBO.getAddressId();
        Integer payMethod = submitOrderBO.getPayMethod();
        String leftMsg = submitOrderBO.getLeftMsg();
        // 邮费默认为0
        Integer postAmount = 0;

        // 查询收货地址
        UserAddress userAddress = addressService.queryUserAddress(userId, addressId);

        // 新订单数据保存
        String orderId = sid.nextShort();
        Orders newOrders = new Orders();
        newOrders.setId(orderId);
        newOrders.setReceiverAddress(userAddress.getProvince() + " " + userAddress.getCity()
                + " " + userAddress.getDistrict() + " " + userAddress.getDetail());
        newOrders.setReceiverMobile(userAddress.getMobile());
        newOrders.setReceiverName(userAddress.getReceiver());

        newOrders.setUserId(userId);
        newOrders.setLeftMsg(leftMsg);
        newOrders.setPostAmount(postAmount);
        newOrders.setPayMethod(payMethod);
        newOrders.setIsDelete(YesOrNo.NO.type);
        newOrders.setIsComment(YesOrNo.NO.type);
        newOrders.setCreatedTime(new Date());
        newOrders.setUpdatedTime(new Date());

        // 循环根据spec 保存订单商品信息
        String itemSpecIdArr[] = itemSpecIds.split(",");
        Integer totalAmount = 0;
        Integer realPayAmount = 0;
        List<ShopcartBO> toBeRemovedShopcartList = new ArrayList<>();
        for (String itemSpecId : itemSpecIdArr) {

            ShopcartBO cart = getcartFromShopcartList(shopcartList, itemSpecId);
            toBeRemovedShopcartList.add(cart);
            Integer buyCount = cart.getBuyCounts();

            ItemsSpec itemsSpec = itemService.queryItemSpecById(itemSpecId);
            totalAmount += itemsSpec.getPriceNormal() * buyCount;
            realPayAmount += itemsSpec.getPriceDiscount();

            String itemId = itemsSpec.getItemId();
            Items item = itemService.queryItemById(itemId);
            String imgUrl = itemService.queryItemMainImgById(itemId);

            // 保存订单详情
            String subOrderId = sid.nextShort();
            OrderItems subOrder = new OrderItems();
            subOrder.setId(subOrderId);
            subOrder.setOrderId(orderId);
            subOrder.setItemId(itemId);
            subOrder.setItemImg(imgUrl);
            subOrder.setItemName(item.getItemName());
            subOrder.setBuyCounts(buyCount);
            subOrder.setItemSpecId(itemSpecId);
            subOrder.setItemSpecName(itemsSpec.getName());
            subOrder.setPrice(itemsSpec.getPriceDiscount());
            orderItemsMapper.insert(subOrder);

            // 扣除库存
            itemService.decreaseItemSpecStock(itemSpecId,buyCount);
        }
        newOrders.setRealPayAmount(realPayAmount);
        newOrders.setTotalAmount(totalAmount);
        ordersMapper.insert(newOrders);

        // 保存订单状态表
        OrderStatus orderStatus = new OrderStatus();
        orderStatus.setOrderId(orderId);
        orderStatus.setOrderStatus(OrderStatusEnum.WAIT_PAY.type);
        orderStatus.setCreatedTime(new Date());

        orderStatusMapper.insert(orderStatus);

        /**
         * 构建请求支付平台生成支付平台订单的参数
         * 回调地址在Controller层写入
         */
        MerchantOrdersVO merchantOrdersVO = new MerchantOrdersVO();
        merchantOrdersVO.setAmount(realPayAmount + postAmount);
        merchantOrdersVO.setMerchantOrderId(orderId);
        merchantOrdersVO.setMerchantUserId(userId);
        merchantOrdersVO.setPayMethod(payMethod);

        OrderVO orderVo = new OrderVO() ;
        orderVo.setOrderId(orderId);
        orderVo.setMerchantOrdersVO(merchantOrdersVO);
        orderVo.setToBeRemovedShopcartList(toBeRemovedShopcartList);
        return orderVo;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void updateOrderStatus(String orderId, Integer orderStatus) {
        OrderStatus updateStatus = new OrderStatus();
        updateStatus.setOrderId(orderId);
        updateStatus.setOrderStatus(orderStatus);
        updateStatus.setPayTime(new Date());
        orderStatusMapper.updateByPrimaryKeySelective(updateStatus);
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public OrderStatus queryOrderStatusInfo(String orderId) {
        return orderStatusMapper.selectByPrimaryKey(orderId);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void closeOrder() {
        // 查询所有未支付的订单，超时时间：1天,超时关闭

        OrderStatus queryOrder = new OrderStatus();
        queryOrder.setOrderStatus(OrderStatusEnum.WAIT_PAY.type);

        List<OrderStatus> list = orderStatusMapper.select(queryOrder);

        for(OrderStatus os : list){
            Date createdTime = os.getCreatedTime();
            int days = DateUtil.daysBetween(createdTime,new Date());
            if(days >= 1){
                doCloseOrder(os.getOrderId());
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    void doCloseOrder(String orderId){
        OrderStatus closeOrder = new OrderStatus();
        closeOrder.setOrderId(orderId);
        closeOrder.setCloseTime(new Date());
        closeOrder.setOrderStatus(OrderStatusEnum.CLOSE.type);
        orderStatusMapper.updateByPrimaryKeySelective(closeOrder);
    }

    /**
     * 从购物车列表获取商品
     *
     * @param shopcartList
     * @param specId
     * @return
     */
    private ShopcartBO getcartFromShopcartList(List<ShopcartBO> shopcartList, String specId){

        for(ShopcartBO cart : shopcartList){
            if(cart.getSpecId().equals(specId)){
                return cart;
            }
        }
        return null;
    }
}
