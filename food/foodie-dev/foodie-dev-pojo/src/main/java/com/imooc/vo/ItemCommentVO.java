package com.imooc.vo;
/**
 * @author hzc
 * @date 2020-06-28 22:25
 */

import lombok.Data;

import java.util.Date;

/**
 * @author hzc
 * @date 2020-06-28 22:25
 */
@Data
public class ItemCommentVO {
    private Integer commentLevel;
    private String content;
    private String specName;
    private Date cecreatedTime;
    private String userFace;
    private String nickname;
}
