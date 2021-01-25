package com.imooc.config;
/**
 * @author hzc
 * @date 2020-06-26 16:30
 */

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * @author hzc
 * @date 2020-06-26 16:30
 */
@Configuration
public class CorsConfig {

    public CorsConfig(){

    }

    @Bean
    public CorsFilter corsFilter(){
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOrigin("http://localhost:8080");
        config.addAllowedOrigin("http://front.zoey.com:8080");
        config.addAllowedOrigin("http://front.zoey.com");
        config.addAllowedOrigin("http://localhost:8080");
        config.addAllowedOrigin("http://localhost:8088");
        config.addAllowedOrigin("http://127.0.0.1:8080");

        // 是否发送cookies
        config.setAllowCredentials(true);
        config.addAllowedMethod("*");

        config.addAllowedHeader("*");

        //映射url
        UrlBasedCorsConfigurationSource corsSource = new UrlBasedCorsConfigurationSource();
        corsSource.registerCorsConfiguration("/**",config);

        return new CorsFilter(corsSource);
    }

}
