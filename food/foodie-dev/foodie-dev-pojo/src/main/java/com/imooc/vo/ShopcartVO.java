package com.imooc.vo;
/**
 * @author hzc
 * @date 2020-06-29 22:39
 */

import lombok.Data;

/**
 * @author hzc
 * @date 2020-06-29 22:39
 */
@Data
public class ShopcartVO {
    private String itemId;
    private String itemImgUrl;
    private String itemName;
    private String specId;
    private String specName;
    private Integer priceDiscount;
    private String priceNormal;
}
