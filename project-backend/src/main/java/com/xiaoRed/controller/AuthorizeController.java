package com.xiaoRed.controller;

import com.xiaoRed.entity.RestBean;
import com.xiaoRed.service.AuthorizeService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.Pattern;
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
    @Resource
    AuthorizeService authorizeService;

    @PostMapping("/valid-email")
    public RestBean<String> validateEmail(@Pattern (regexp = EMAIL_REGEX) @RequestParam("email") String email,
                                          HttpSession session){
        //需要传入Session的id，不然换个邮箱就绕过我设置的60秒冷却时间
        if(authorizeService.sendValidateEmail(email, session.getId()))
            return RestBean.success("邮件已发送，请注意查收");
        else
            return RestBean.failure(400,"邮件发送失败，请联系管理员");
    }
}
