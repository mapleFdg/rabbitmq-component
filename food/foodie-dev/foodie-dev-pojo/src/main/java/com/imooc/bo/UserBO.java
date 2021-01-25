package com.imooc.bo;
/**
 * @author hzc
 * @date 2020-06-26 15:06
 */

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author hzc
 * @date 2020-06-26 15:06
 */
@Data
@ApiModel(value = "用户对象BO",description = "从客户端，由用户传入的数据封装")
public class UserBO {

    @ApiModelProperty(value = "用户名" , example = "maple",required = true)
    private String username;
    @ApiModelProperty(value = "密码" , example = "123123",required = true)
    private String password;
    @ApiModelProperty(value = "确认密码" , example = "123123",required = false)
    private String confirmPassword;

}
