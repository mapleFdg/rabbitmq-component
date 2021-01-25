package com.imooc.config;
/**
 * @author hzc
 * @date 2020-07-05 18:06
 */

import com.imooc.interceptor.UserTokenInterceptor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author hzc
 * @date 2020-07-05 18:06
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * Rest请求类
     * @param builder
     * @return
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder){
        return builder.build();
    }

    /**
     * token 拦截器
     *
     * @return
     */
    @Bean
    public UserTokenInterceptor userTokenInterceptor(){
        return new UserTokenInterceptor();
    }


    /**
     * 实现静态资源注册
     * @param registry
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 映射swagger2
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/META-INF/resources/")
                .addResourceLocations("file:/Users/hzc/Project/idea-muke-jiagou/foodie/");
    }

    /**
     * 添加拦截器
     *
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userTokenInterceptor())
                .addPathPatterns("/address/*")
                .addPathPatterns("/orders/*")
                .addPathPatterns("/shopcart/*")
                .addPathPatterns("/center/*")
                .addPathPatterns("/userInfo/*")
                .addPathPatterns("/mycomments/*")
                .addPathPatterns("/myorders/*")
                .excludePathPatterns("/orders/notifyMerchantOrderPaid")
                .excludePathPatterns("/myorders/deliver");
        WebMvcConfigurer.super.addInterceptors(registry);
    }
}
