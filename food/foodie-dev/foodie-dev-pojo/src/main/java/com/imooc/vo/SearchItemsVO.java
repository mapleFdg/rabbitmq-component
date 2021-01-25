package com.imooc.vo;
/**
 * @author hzc
 * @date 2020-06-29 22:02
 */

import lombok.Data;

/**
 * @author hzc
 * @date 2020-06-29 22:02
 */
@Data
public class SearchItemsVO {
    private String itemId;
    private String itemName;
    private Integer sellCounts;
    private String imgUrl;
    private Integer price;
}
