package com.imooc.vo;
/**
 * @author hzc
 * @date 2020-06-27 14:48
 */

import lombok.Data;

import java.util.List;

/**
 * @author hzc
 * @date 2020-06-27 14:48
 */
@Data
public class NewItemsVO {

    private Integer rootCatId;
    private String rootCatName;
    private String slogan;
    private String catImage;
    private String bgColor;

    private List<SimpleItemVO> simpleItemList;
}
