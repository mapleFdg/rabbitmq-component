package com.imooc.vo;
/**
 * @author hzc
 * @date 2020-06-27 13:58
 */

import lombok.Data;

/**
 * @author hzc
 * @date 2020-06-27 13:58
 */
@Data
public class SubCategoryVO {

    private Integer subId;

    private String subName;

    private String subType;

    private Integer subFatherId;
}
