package com.xiaoRed.controller;

import com.xiaoRed.entity.RestBean;
import com.xiaoRed.service.AuthorizeService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
@Validated//开启验证：虽然前端对email的合法性做过验证了，但是为了安全，后端还是要再做一次验证（前端总是不靠谱的）
@RestController
@RequestMapping("/api/auth")
public class AuthorizeController {

    //邮件地址的正则表达式
    private final String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+.[a-zA-Z]{2,}$";
    //包含中英文，不含特殊字符的用户名的正则表达式
    private final String USERNAME_REGEX = "^[a-zA-Z0-9\\u4e00-\\u9fa5]+$";
    @Resource
    AuthorizeService authorizeService;

    //发送验证码
    @PostMapping("/valid-email")
    public RestBean<String> validateEmail(@Pattern (regexp = EMAIL_REGEX) @RequestParam("email") String email,
                                          HttpSession session){
        //需要传入Session的id，不然换个邮箱就绕过我设置的60秒冷却时间
        String s = authorizeService.sendValidateEmail(email, session.getId());
        if(s == null)
            return RestBean.success("邮件已发送，请注意查收");
        else
            return RestBean.failure(400,s);
    }

    //验证并注册
    @PostMapping("/register")
    public RestBean<String> registerUser(@Pattern(regexp = USERNAME_REGEX)@Length(min = 2, max =8) @RequestParam("username") String username,
                                         @Length(min = 6, max =16) @RequestParam("password") String password,
                                         @Pattern (regexp = EMAIL_REGEX) @RequestParam("email") String email,
                                         @Length(min = 6, max =6)@RequestParam("code") String code,
                                         HttpSession session){
        String s = authorizeService.validateAndRegister(username, password, email, code, session.getId());
        if(s == null)
            return RestBean.success("注册成功");
        else
            return RestBean.failure(400, s);
    }
}
