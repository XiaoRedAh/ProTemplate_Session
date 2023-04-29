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

    //注册功能发送验证码
    @PostMapping("/valid-register-email")
    public RestBean<String> validateRegisterEmail(@Pattern (regexp = EMAIL_REGEX) @RequestParam("email") String email,
                                          HttpSession session){
        //需要传入Session的id，不然换个邮箱就绕过我设置的60秒冷却时间
        //注册功能发送验证码要求传入的邮箱是未注册的，因此传入false
        String s = authorizeService.sendValidateEmail(email, session.getId(), false);
        if(s == null)
            return RestBean.success("邮件已发送，请注意查收");
        else
            return RestBean.failure(400,s);
    }

    //重置密码功能发送验证码
    @PostMapping("/valid-reset-email")
    public RestBean<String> validateResetEmail(@Pattern (regexp = EMAIL_REGEX) @RequestParam("email") String email,
                                          HttpSession session){
        //需要传入Session的id，不然换个邮箱就绕过我设置的60秒冷却时间
        //重置密码功能发送验证码要求传入的邮箱是已注册的，因此传入true
        String s = authorizeService.sendValidateEmail(email, session.getId(), true);
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

    /**
     * 1. 发送邮件
     * 2. redis中查验证码是否正确，正确就在Session中存一个标记
     * 3. 用户发起重置密码请求，如果存在标记，就成功重置
     */
    //重置密码第一个步骤：通过邮箱认证
    @PostMapping("/start-reset")
    public RestBean<String> startReset(@Pattern (regexp = EMAIL_REGEX) @RequestParam("email") String email,
                                       @Length(min = 6, max =6)@RequestParam("code") String code,
                                       HttpSession session){
        String s = authorizeService.validateOnly(email, code, session.getId());
        if(s == null){
            //验证成功，往session里存要重置密码的账户
            session.setAttribute("reset-password", email);
            return RestBean.success();
        }else{
            return RestBean.failure(400,s);
        }
    }
    //重置密码第二个步骤：认证通过，重置密码
    @PostMapping("do-reset")
    public RestBean<String> doRest(@Length(min = 6, max =16) @RequestParam("password") String password,
                                   HttpSession session){
        //取session里的reset-password
        String email = (String) session.getAttribute("reset-password");
        if(email == null){//如果取出来为空，说明没有做验证
            return RestBean.failure(401,"请先完成邮箱验证");
        }else if(authorizeService.resetPassword(password, email)){//认证成功，则重置密码
            session.removeAttribute("reset-password");//重置成功后，也要把存在session里的东西删掉
            return RestBean.success("密码重置成功");
        }else{
            return RestBean.failure(500, "内部错误，请联系管理员");
        }
    }

}
