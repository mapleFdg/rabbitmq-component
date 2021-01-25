package com.imooc.service.center;

import com.imooc.bo.center.CenterUserBo;
import com.imooc.pojo.Users;

/**
 * @author hzc
 * @date 2020-07-06 23:31
 */
public interface CenterUserService {

    public Users queryUserInfo(String userId);

    public Users updateUserInfo(String userId, CenterUserBo centerUserBo);

    public Users updateUserFace(String userId, String faceUrl);
}
