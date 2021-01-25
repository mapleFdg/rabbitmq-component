package com.imooc.config;
/**
 * @author hzc
 * @date 2020-07-05 22:17
 */

import com.imooc.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author hzc
 * @date 2020-07-05 22:17
 */
@Component
public class OrderJob {

    @Autowired
    private OrderService orderService;

    /**
     * 定时关闭订单
     */
    //@Scheduled(cron = "0/3 * * * * ? ")
    public void autCloseOrder(){
        orderService.closeOrder();
    }

}
