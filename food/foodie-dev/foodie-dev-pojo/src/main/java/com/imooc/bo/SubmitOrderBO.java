package com.imooc.bo;
/**
 * @author hzc
 * @date 2020-07-05 10:06
 */

import com.imooc.validator.PayMethod;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author hzc
 * @date 2020-07-05 10:06
 */
@Data
public class SubmitOrderBO {

    @NotBlank(message = "用户id不能为空")
    private String userId;

    private String itemSpecIds;
    @NotBlank(message = "地址ID不能为空")
    private String addressId;

    @PayMethod(message = "不支持该类型的支付方式")
    private Integer payMethod;
    private String leftMsg;

}
