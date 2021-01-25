package com.imooc.vo;

import lombok.Data;

/**
 * @author hzc
 * @date 2020/10/24 2:33 下午
 */
@Data
public class UserVO {

    /**
     * 主键id 用户id
     */
    private String id;

    /**
     * 用户名 用户名
     */
    private String username;

    /**
     * 昵称 昵称
     */
    private String nickname;

    /**
     * 头像 头像
     */
    private String face;

    /**
     * 性别 性别 1:男  0:女  2:保密
     */
    private Integer sex;

    // 用户会话token
    private String userUniqueToken;
}
