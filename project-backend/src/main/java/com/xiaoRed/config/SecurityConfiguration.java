package com.xiaoRed.config;

import com.alibaba.fastjson.JSONObject;
import com.xiaoRed.entity.RestBean;
import com.xiaoRed.service.AuthorizeService;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.sql.DataSource;
import java.io.IOException;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Resource
    AuthorizeService authorizeService;

    @Resource
    DataSource dataSource;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           PersistentTokenRepository repository) throws Exception {
        return http.
                authorizeHttpRequests()
                .requestMatchers("/api/auth/**").permitAll()//验证相关的请求全部放行
                .anyRequest().authenticated()
                .and()
                .formLogin()
                .loginProcessingUrl("/api/auth/login")
                .successHandler(this::onAuthenticationSuccess)//登录成功时的操作
                .failureHandler(this::onAuthenticationFailure)//登录失败时的操作
                .and()
                .logout()
                .logoutUrl("/api/auth/logout")
                .logoutSuccessHandler(this::onAuthenticationSuccess)//退出登录时的操作
                .and()
                .rememberMe()//配置“记住我功能”
                .rememberMeParameter("remember")//参数默认是remember-me,但我的前端写的是remember，因此要配置一下
                .tokenRepository(repository)//持久化存储rememberMe的token，这里使用JDBC存储
                .tokenValiditySeconds(3600 * 24 * 7)//“记住我”token的有效时间设置为7天：3600秒*24小时*7天
                .and()
                .csrf()
                .disable()
                .cors()//配置跨域相关
                .configurationSource(this.corsConfigurationSource())
                .and()
                .exceptionHandling()
                .authenticationEntryPoint(this::onAuthenticationFailure)//发生异常时的操作（一般是没权限）
                .and()
                .build();
    }

    //使用JDBC持久化存储rememberMe的token
    @Bean
    public PersistentTokenRepository tokenRepository(){
        JdbcTokenRepositoryImpl jdbcTokenRepository=new JdbcTokenRepositoryImpl();
        jdbcTokenRepository.setDataSource(dataSource);//配置数据源
        jdbcTokenRepository.setCreateTableOnStartup(false);//首次启动设置为true来创建表，之后设置为false就行
        return jdbcTokenRepository;
    }
    //跨域配置源
    private CorsConfigurationSource corsConfigurationSource(){
        CorsConfiguration cors = new CorsConfiguration();
        cors.addAllowedOriginPattern("*");//测试的时候可以允许所有的跨域请求，实际上为了安全应该设置为只允许自己服务器的前端
        cors.setAllowCredentials(true);//允许携带cookie
        cors.addAllowedHeader("*");
        cors.addAllowedMethod("*");
        cors.addExposedHeader("*");
        //封装成配置源
        UrlBasedCorsConfigurationSource source =new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**",cors);//注册配置源：所有请求走上面自定义的跨域政策
        return source;
    }
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity security) throws Exception {
        //使用自定义的验证服务
        return security
                .getSharedObject(AuthenticationManagerBuilder.class)
                .userDetailsService(authorizeService)
                .and()
                .build();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        response.setCharacterEncoding(("utf-8"));
        //将登录成功的响应转换为json格式响应回去
        if(request.getRequestURI().endsWith("/login"))
          response.getWriter().write(JSONObject.toJSONString(RestBean.success("登录成功")));
        //将退出登录成功的响应转换为json格式响应回去
        else if(request.getRequestURI().endsWith("/logout"))
            response.getWriter().write(JSONObject.toJSONString(RestBean.success("退出登录成功")));
    }

    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        response.setCharacterEncoding(("utf-8"));
        //将登录失败或发生异常（没权限。。。）的响应转换成json格式响应回去
        response.getWriter().write(JSONObject.toJSONString(RestBean.failure(401,exception.getMessage())));
    }

}
