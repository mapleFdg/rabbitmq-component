package com.imooc.vo;
/**
 * @author hzc
 * @date 2020-06-28 22:07
 */

import lombok.Data;

/**
 * @author hzc
 * @date 2020-06-28 22:07
 */
@Data
public class CommentLevelCountsVO {

    private Integer totalCounts;
    private Integer goodCounts;
    private Integer normalCounts;
    private Integer badCounts;

}
