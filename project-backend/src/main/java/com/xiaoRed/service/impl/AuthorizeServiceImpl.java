package com.xiaoRed.service.impl;

import com.xiaoRed.entity.Account;
import com.xiaoRed.mapper.UserMapper;
import com.xiaoRed.service.AuthorizeService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class AuthorizeServiceImpl implements AuthorizeService {

    @Value("${spring.mail.username}")
    String from;
    @Resource
    UserMapper userMapper;

    @Resource
    MailSender mailSender;

    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if(username==null)
            throw new UsernameNotFoundException("用户名不能为空");
        Account account=userMapper.findAccountByNameOrEmail(username);
        if(account==null)
            throw new UsernameNotFoundException("用户名或密码错误");
        return User
                .withUsername(account.getUsername())
                .password(account.getPassword())
                .roles("user")
                .build();
    }

    /**
     * 1. 生成存放在Redis中的key。先生成出对应的验证码
     * 2. 拿这个key去Redis里面找，如果有这个键值对，那么剩余时间低于2分钟才可以重发验证码，否则不能发送验证码，直接返回false
     * 3. 如果通过上面的判断，则生成验证码，发送验证码到指定邮箱
     * 4. 邮箱和对应的验证码存放到Redis里面，设置过期时间是3分钟。如果发送失败，把Redis里面刚刚插入的删去
     * 5. 用户在注册时，再从Redis里面取出对应键值对，看验证码是否一致
     */
    @Override
    public boolean sendValidateEmail(String email, String sessionId) {
        //生成存放在Redis中的key
        String key = "email:" + sessionId + ":" + email;
        //生成验证码前，先判断Redis中是否以已经有这个key了
        if(Boolean.TRUE.equals(stringRedisTemplate.hasKey(key))){
            //如果剩余时间大于两分钟，那么不能发送邮件（0L是应付代码走到这，Redis对应key的键值对刚好过期的情况）
            Long expire = Optional.ofNullable(stringRedisTemplate.getExpire(key, TimeUnit.SECONDS)).orElse(0L);
            if(expire > 120)return false;
        }
        //生成6位验证码
        Random random =new Random();
        int code = random.nextInt(899999) + 100000;//这样保证生成的code一定是6位数
        //封装要发送的邮件
        SimpleMailMessage message=new SimpleMailMessage();
        message.setFrom(from);//配置发送邮件的邮箱
        message.setTo(email);//发送邮件给注册用户填写的邮箱地址
        message.setSubject("您的验证邮箱");//发出去的邮件的标题
        message.setText("验证码："+code);//邮件内容
        try{
            //发送包含验证码的邮件给注册用户填写的邮箱
            mailSender.send(message);
            //往Redis数据库存键值对，有效期是3分钟
            stringRedisTemplate.opsForValue().set(key, String.valueOf(code), 3, TimeUnit.MINUTES);
            return true;
        }catch (MailException e){
            e.printStackTrace();
            return false;
        }
    }
}
