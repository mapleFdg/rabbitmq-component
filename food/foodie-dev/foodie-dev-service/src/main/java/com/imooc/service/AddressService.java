package com.imooc.service;

import com.imooc.bo.AddressBO;
import com.imooc.pojo.UserAddress;

import java.util.List;

/**
 * @author hzc
 * @date 2020-07-02 23:10
 */
public interface AddressService {

    public List<UserAddress> queryAll(String userId);

    public void addNewUserAddress(AddressBO addressBO);

    /**
     * 用户修改地址
     * @param addressBO
     */
    public void updateUserAddress(AddressBO addressBO);

    /**
     * 根据用户id和地址id，删除对应的用户地址信息
     * @param userId
     * @param addressId
     */
    public void deleteUserAddress(String userId, String addressId);

    /**
     * 修改默认地址
     * @param userId
     * @param addressId
     */
    public void updateUserAddressToBeDefault(String userId, String addressId);

    /**
     * 根据用户ID和地址ID查询送货信息
     *
     * @param userId
     * @param addressId
     * @return
     */
    public UserAddress queryUserAddress(String userId,String addressId);

}
