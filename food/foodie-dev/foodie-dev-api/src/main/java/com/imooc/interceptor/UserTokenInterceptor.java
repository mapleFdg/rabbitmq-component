package com.imooc.interceptor;

import com.imooc.utils.JSONResult;
import com.imooc.utils.JsonUtils;
import com.imooc.utils.RedisOperator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

/**
 * token 拦截器
 *
 * @author hzc
 * @date 2020/10/24 6:06 下午
 */
@Slf4j
public class UserTokenInterceptor implements HandlerInterceptor {

    public static final String REDIS_USER_TOKEN = "redis_user_token";

    @Autowired
    private RedisOperator redisOperator;

    /**
     * 访问前拦截请求
     *
     * @param request
     * @param response
     * @param handler
     * @return false：请求被拦截  true：请求通过
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String userToken = request.getHeader("headerUserToken");
        String userId = request.getHeader("headerUserId");

        if (StringUtils.isNotBlank(userToken) && StringUtils.isNotBlank(userId)) {
            String uniqueToken = redisOperator.get(REDIS_USER_TOKEN + ":" + userId);
            if (StringUtils.isBlank(uniqueToken)) {
                log.info("token is null");
                returnErrorResult(response,JSONResult.errorTokenMsg("请登录"));
                return false;
            } else {
                if (!uniqueToken.equals(userToken)) {
                    log.info("token is wrong");
                    returnErrorResult(response,JSONResult.errorTokenMsg("请登录"));
                    return false;
                }
            }
        } else {
            log.info("token is null");
            returnErrorResult(response,JSONResult.errorTokenMsg("请登录"));
            return false;
        }
        return true;
    }

    public void returnErrorResult(HttpServletResponse response, JSONResult result){
        try(OutputStream out = response.getOutputStream()){
            response.setCharacterEncoding("utf-8");
            response.setContentType("text/json");
            out.write(JsonUtils.objectToJson(result).getBytes("utf-8"));
            out.flush();
        }catch (IOException exception){
            exception.printStackTrace();
        }

    }


    /**
     * 请求Controller之后，渲染视图之前
     *
     * @param request
     * @param response
     * @param handler
     * @param modelAndView
     * @throws Exception
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    /**
     * 请求Controller之后，渲染视图之后
     *
     * @param request
     * @param response
     * @param handler
     * @param ex
     * @throws Exception
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }
}
