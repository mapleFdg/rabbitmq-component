package com.imooc.bo;
/**
 * @author hzc
 * @date 2020-07-02 23:13
 */

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author hzc
 * @date 2020-07-02 23:13
 */
@Data
@ApiModel
public class AddressBO {

    @ApiModelProperty(value = "地址ID" , name = "addressId", required = false)
    private String addressId;

    private String userId;
    @ApiModelProperty(value = "收货人" , name = "receiver", required = true)
    private String receiver;
    private String mobile;
    private String province;
    private String city;
    private String district;
    private String detail;

}
