package com.imooc.vo;
/**
 * @author hzc
 * @date 2020-06-27 13:57
 */

import lombok.Data;

import java.util.List;

/**
 * 二级分类VO
 *
 * @author hzc
 * @date 2020-06-27 13:57
 */
@Data
public class CategoryVO {

    private Integer id;

    private String name;

    private String type;

    private Integer fatherId;

    private List<SubCategoryVO> subCatList;


}
