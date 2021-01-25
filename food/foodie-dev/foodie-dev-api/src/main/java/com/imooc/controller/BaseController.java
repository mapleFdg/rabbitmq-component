package com.imooc.controller;

import com.imooc.pojo.Users;
import com.imooc.utils.RedisOperator;
import com.imooc.vo.UserVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author hzc
 * @date 2020-06-22 22:19
 */
public class BaseController {

    public static final String SHOP_CART = "shopcart";
    public static final String REDIS_USER_TOKEN = "redis_user_token";

    public static final Integer COMMON_PAGE_SIZE = 10;
    public static final Integer PAGE_SIZE = 20;

    String paymentUrl = "http://payment.t.mukewang.com/foodie-payment/payment/createMerchantOrder";
    public static final String PAY_RETURN_URL = "https://nat.mynatapp.cc/orders/notifyMerchantOrderPaid";

    public static final String PAY_USER = "1106109-362821775";
    public static final String PAY_PASSWORD = "r328-90ru-089j-r49k";


    @Autowired
    private RedisOperator redisOperator;

    /**
     * /Users/hzc/Project/idea-muke-jiagou/foodie/face
     *
     * 头像文件上传地址
     */
    public static final String IMAGE_USER_FACE_LOCATION = File.separator + "Users" + File.separator + "hzc"
            + File.separator + "Project" + File.separator + "idea-muke-jiagou"
            + File.separator + "foodie" + File.separator + "face" + File.separator;


    protected Map<String, String> getErrors(BindingResult result) {
        Map<String, String> errorMap = new HashMap<>();
        List<FieldError> errorList = result.getFieldErrors();
        for (FieldError fieldError : errorList) {
            String field = fieldError.getField();
            String errorMsg = fieldError.getDefaultMessage();
            errorMap.put(field, errorMsg);
        }
        return errorMap;
    }

    public UserVO conventUserVO(Users user){
        String uniqueToken = UUID.randomUUID().toString().trim();
        redisOperator.set(REDIS_USER_TOKEN + ":" + user.getId(),uniqueToken);

        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user,userVO);
        userVO.setUserUniqueToken(uniqueToken);
        return userVO;
    }

}
