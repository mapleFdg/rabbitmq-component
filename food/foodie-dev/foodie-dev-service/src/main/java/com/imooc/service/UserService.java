package com.imooc.service;

import com.imooc.bo.UserBO;
import com.imooc.pojo.Users;

/**
 * @author hzc
 * @date 2020-06-26 14:16
 */
public interface UserService {

    /**
     * 判断用户名是否存在
     *
     * @param username
     * @return
     */
    public boolean queryUsernameIsExist(String username);

    /**
     *
     * 创建用户
     *
     * @param userBO
     * @return
     */
    public Users createUser(UserBO userBO);

    public Users queryUserForLogin(String username, String password);
}
