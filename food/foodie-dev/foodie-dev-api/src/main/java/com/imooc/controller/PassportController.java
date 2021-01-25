package com.imooc.controller;
/**
 * @author hzc
 * @date 2020-06-26 14:23
 */

import com.imooc.bo.ShopcartBO;
import com.imooc.bo.UserBO;
import com.imooc.pojo.Users;
import com.imooc.service.UserService;
import com.imooc.utils.*;
import com.imooc.vo.UserVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author hzc
 * @date 2020-06-26 14:23
 */
@Api(value = "注册登录", tags = {"用于注册登录相关接口"})
@RestController
@RequestMapping("passport")
public class PassportController extends BaseController{

    @Autowired
    private UserService userService;

    @Autowired
    private RedisOperator redisOperator;

    @ApiOperation(value = "用户名是否存在", notes = "用户名是否存在", httpMethod = "GET")
    @GetMapping("/usernameIsExist")
    public JSONResult usernameIsExist(@RequestParam String username){

        if(StringUtils.isBlank(username)){
            return JSONResult.errorMsg("用户名不能为空");
        }

        boolean isExit = userService.queryUsernameIsExist(username);
        if(isExit){
            return JSONResult.errorMsg("用户名已存在");
        }

        return JSONResult.ok();
    }

    @ApiOperation(value = "用户注册", notes = "用户注册", httpMethod = "POST")
    @PostMapping("/regist")
    public JSONResult regist(@RequestBody UserBO userBO, HttpServletRequest request, HttpServletResponse response) {

        String username = userBO.getUsername();
        String password = userBO.getPassword();
        String confirmPwd = userBO.getConfirmPassword();

        // 0. 判断用户名和密码必须不为空
        if (StringUtils.isBlank(username) ||
                StringUtils.isBlank(password) ||
                StringUtils.isBlank(confirmPwd)) {
            return JSONResult.errorMsg("用户名或密码不能为空");
        }

        // 1. 查询用户名是否存在
        boolean isExist = userService.queryUsernameIsExist(username);
        if (isExist) {
            return JSONResult.errorMsg("用户名已经存在");
        }

        // 2. 密码长度不能少于6位
        if (password.length() < 6) {
            return JSONResult.errorMsg("密码长度不能少于6");
        }

        // 3. 判断两次密码是否一致
        if (!password.equals(confirmPwd)) {
            return JSONResult.errorMsg("两次密码输入不一致");
        }

        // 4. 实现注册
        Users userResult = userService.createUser(userBO);

        UserVO userVO = conventUserVO(userResult);

        CookieUtils.setCookie(request, response, "user",
                JsonUtils.objectToJson(userVO), true);

        syncShopcartData(request,response,userResult.getId());

        return JSONResult.ok(userResult);
    }

    @ApiOperation(value = "用户登录", notes = "用户登录", httpMethod = "POST")
    @PostMapping("/login")
    public JSONResult login(@RequestBody UserBO userBO, HttpServletRequest request, HttpServletResponse response) throws Exception{

        String username = userBO.getUsername();
        String password = userBO.getPassword();

        // 0. 判断用户名和密码必须不为空
        if (StringUtils.isBlank(username) ||
                StringUtils.isBlank(password)) {
            return JSONResult.errorMsg("用户名或密码不能为空");
        }

        // 1. 实现登录
        Users userResult = userService.queryUserForLogin(username,
                MD5Utils.getMD5Str(password));

        if (userResult == null) {
            return JSONResult.errorMsg("用户名或密码不正确");
        }

        UserVO userVO = conventUserVO(userResult);

        CookieUtils.setCookie(request, response, "user",
                JsonUtils.objectToJson(userVO), true);

        syncShopcartData(request,response,userVO.getId());

        return JSONResult.ok(userVO);
    }

    @ApiOperation(value = "用户退出登录", notes = "用户退出登录", httpMethod = "POST")
    @PostMapping("/logout")
    public JSONResult logout(@RequestParam String userId, HttpServletRequest request, HttpServletResponse response) {

        // 清除用户的相关信息的cookie
        CookieUtils.deleteCookie(request, response, "user");

        redisOperator.del(REDIS_USER_TOKEN + ":" + userId);

        CookieUtils.deleteCookie(request,response,SHOP_CART);

        return JSONResult.ok();
    }

    /**
     * 同步redis与cookies购物车信息（参考京东）
     *
     * @param response
     * @param request
     * @param userId
     */
    private void syncShopcartData(HttpServletRequest request,HttpServletResponse response,String userId){

        String shopcartRedisJson = redisOperator.get(SHOP_CART + ":" + userId);

        String shopcartCookiesJson = CookieUtils.getCookieValue(request,SHOP_CART,true);

        if(StringUtils.isBlank(shopcartRedisJson)){
            if(StringUtils.isNotBlank(shopcartCookiesJson)){
                redisOperator.set(SHOP_CART + ":" + userId,shopcartCookiesJson);
            }
        }else{
            if(StringUtils.isBlank(shopcartCookiesJson)){
                CookieUtils.setCookie(request,response,SHOP_CART,shopcartRedisJson,true);
            }else{
                List<ShopcartBO> shopcartRedis = JsonUtils.jsonToList(shopcartRedisJson,ShopcartBO.class);
                List<ShopcartBO> shopcartCookies = JsonUtils.jsonToList(shopcartCookiesJson,ShopcartBO.class);
                List<ShopcartBO> removedCart = new ArrayList<>();
                for(ShopcartBO cart : shopcartCookies){
                    String specId = cart.getSpecId();
                    for(ShopcartBO redisCart : shopcartRedis){
                        if(specId.equals(redisCart.getSpecId())){
                            redisCart.setBuyCounts(cart.getBuyCounts());
                            removedCart.add(cart);
                            break;
                        }
                    }
                }
                shopcartCookies.removeAll(removedCart);
                shopcartRedis.addAll(shopcartCookies);
                redisOperator.set(SHOP_CART + ":" + userId,JsonUtils.objectToJson(shopcartRedis));
                CookieUtils.setCookie(request,response,SHOP_CART,JsonUtils.objectToJson(shopcartRedis),true);
            }
        }

    }
}
