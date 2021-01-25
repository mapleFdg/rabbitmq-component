package com.imooc.vo;
/**
 * @author hzc
 * @date 2020-07-05 17:50
 */

import com.imooc.bo.ShopcartBO;
import lombok.Data;

import java.util.List;

/**
 * @author hzc
 * @date 2020-07-05 17:50
 */
@Data
public class OrderVO {

    private String orderId;
    private MerchantOrdersVO merchantOrdersVO;
    private List<ShopcartBO> toBeRemovedShopcartList;

}
