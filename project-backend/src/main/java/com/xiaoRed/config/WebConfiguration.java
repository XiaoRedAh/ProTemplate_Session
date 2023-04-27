package com.xiaoRed.config;

import com.xiaoRed.interceptor.AuthorizeInterceptor;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfiguration implements WebMvcConfigurer {
    @Resource
    AuthorizeInterceptor authorizeInterceptor;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry
                .addInterceptor(authorizeInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/api/auth/**");//对于api/auth路径下的所有请求放行（注册，获取验证码功能才不会被拦截）
    }
}
