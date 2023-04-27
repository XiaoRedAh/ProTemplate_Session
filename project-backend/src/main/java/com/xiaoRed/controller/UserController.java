package com.xiaoRed.controller;

import com.xiaoRed.entity.RestBean;
import com.xiaoRed.entity.user.AccountUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

@RestController
@RequestMapping("/api/user")
public class UserController {
    /**
     * prehandle()方法将登录成功的用户详细信息存放在请求体的session中
     * 调用这个接口时，从请求体中拿到用户详细信息
     * 然后直接将它返回给前端，前端就可以很方便地操纵这些信息了
     */
    @GetMapping("/me")
    public RestBean<AccountUser> me(@SessionAttribute("accountUser") AccountUser accountUser){
        return RestBean.success(accountUser);
    }
}
