package com.xiaoRed.interceptor;

import com.xiaoRed.entity.user.AccountUser;
import com.xiaoRed.mapper.UserMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
@Component
public class AuthorizeInterceptor implements HandlerInterceptor {
    @Resource
    UserMapper userMapper;

    /**
     * 请求到来之前做处理
     * 不用担心用户没登录，因为如果没登录，在security的过滤器就已经被拦截了
     *
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //从SecurityContext中拿到登录用户的用户信息
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        User user = (User) authentication.getPrincipal();
        String username = user.getUsername();
        AccountUser accountUser = userMapper.findAccountUserByNameOrEmail(username);//一定是可以查到的
        request.getSession().setAttribute("accountUser", accountUser);//往请求体里的session域里丢用户详细信息，让Controller层拿到它
        return true;
    }
}
