package com.imooc.controller;
/**
 * @author hzc
 * @date 2020-06-29 22:33
 */

import com.imooc.bo.ShopcartBO;
import com.imooc.utils.JSONResult;
import com.imooc.utils.JsonUtils;
import com.imooc.utils.RedisOperator;
import com.imooc.vo.ShopcartVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * @author hzc
 * @date 2020-06-29 22:33
 */
@Api(value = "购物车接口", tags = {"购物车接口"})
@RestController
@RequestMapping("shopcart")
public class ShopCatController extends BaseController{

    @Autowired
    private RedisOperator redisOperator;

    @ApiOperation(value = "添加购物车",notes = "添加购物车")
    @PostMapping("/add")
    public JSONResult add(
            @ApiParam(name = "userId",value = "用户ID",required = true)
            @RequestParam String userId,
            @RequestBody ShopcartBO shopcartBO,
            HttpServletRequest request,
            HttpServletResponse response){

        if(StringUtils.isBlank(userId)){
            return JSONResult.errorMsg("");
        }

        List<ShopcartBO> shopcartBOList = null;
        String shopcartBOListStr = redisOperator.get(SHOP_CART + ":" + userId);
        if(StringUtils.isNotBlank(shopcartBOListStr)){
            shopcartBOList = JsonUtils.jsonToList(shopcartBOListStr,ShopcartBO.class);
            boolean isHaving = false;
            for(ShopcartBO s: shopcartBOList){
                if(s.getSpecId().equals(shopcartBO.getSpecId())){
                    s.setBuyCounts(s.getBuyCounts() + shopcartBO.getBuyCounts());
                    isHaving = true;
                }
            }
            if(!isHaving){
                shopcartBOList.add(shopcartBO);
            }
        }else{
            shopcartBOList = new ArrayList<>();
            shopcartBOList.add(shopcartBO);
        }

        redisOperator.set(SHOP_CART + ":" + userId, JsonUtils.objectToJson(shopcartBOList));

        return JSONResult.ok();
    }

    @ApiOperation(value = "删除购物车",notes = "删除购物车")
    @PostMapping("/del")
    public JSONResult del(
            @ApiParam(name = "userId",value = "用户ID",required = true)
            @RequestParam String userId,
            @RequestParam String itemSpecId,
            HttpServletRequest request,
            HttpServletResponse response){
        if(StringUtils.isBlank(userId) || StringUtils.isBlank(itemSpecId)){
            return JSONResult.errorMsg("参数不能为空");
        }

        String shopCatJson = redisOperator.get(SHOP_CART + ":" + userId);
        if(StringUtils.isNotBlank(shopCatJson)){
            List<ShopcartBO> shopcartBOS = JsonUtils.jsonToList(shopCatJson,ShopcartBO.class);
            for(ShopcartBO shopcartBO : shopcartBOS){
                if(shopcartBO.getSpecId().equals(itemSpecId)){
                    shopcartBOS.remove(shopcartBO);
                    break;
                }
            }
            redisOperator.set(SHOP_CART + ":" + userId,JsonUtils.objectToJson(shopcartBOS));
        }
        return JSONResult.ok();
    }


}
