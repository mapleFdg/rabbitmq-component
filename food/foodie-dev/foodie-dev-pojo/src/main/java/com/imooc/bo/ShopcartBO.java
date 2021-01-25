package com.imooc.bo;
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
public class ShopcartBO {
    private String itemId;
    private String itemImgUrl;
    private String itemName;
    private String specId;
    private String specName;
    private Integer buyCounts;
    private Integer priceDiscount;
    private String priceNormal;
}
