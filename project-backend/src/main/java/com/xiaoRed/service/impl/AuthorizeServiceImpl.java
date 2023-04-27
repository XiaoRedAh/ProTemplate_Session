package com.xiaoRed.service.impl;

import com.xiaoRed.entity.auth.Account;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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

    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

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
     * 3. 如果邮箱已经注册过，也不能成功发送验证码
     * 4. 如果通过上面的判断，则生成验证码，发送验证码到指定邮箱
     * 5. 邮箱和对应的验证码存放到Redis里面，设置过期时间是3分钟。如果发送失败，把Redis里面刚刚插入的删去
     * 6. 用户在注册时，再从Redis里面取出对应键值对，看验证码是否一致
     */
    @Override
    public String sendValidateEmail(String email, String sessionId, boolean hasAccount) {
        //生成存放在Redis中的key,要求带上hasaccount，防止有人用重置密码的验证码去填注册的验证码（或反过来）
        String key = "email:" + sessionId + ":" + email + ":" + hasAccount;
        //生成验证码前，先判断Redis中是否以已经有这个key了
        if(Boolean.TRUE.equals(stringRedisTemplate.hasKey(key))){
            //如果剩余时间大于两分钟，那么不能发送邮件（0L是应付代码走到这，Redis对应key的键值对刚好过期的情况）
            Long expire = Optional.ofNullable(stringRedisTemplate.getExpire(key, TimeUnit.SECONDS)).orElse(0L);
            if(expire > 120)return "请求频繁，请稍后再试";
        }
        Account account = userMapper.findAccountByNameOrEmail(email);
        //重置密码功能的发送验证码：需要邮箱是已注册的，但却查到空，说明传入的邮箱未注册，不能发送验证码
        if(hasAccount && account==null) return "没有此邮箱地址的账户";
        //注册功能的发送验证码：需要邮箱是未注册的，但却查到了用户，说明传入的邮箱已注册过，不能发送验证码
        if(!hasAccount && account !=null) return "此邮箱已被其他用户注册";
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
            return null;
        }catch (MailException e){
            e.printStackTrace();
            return "邮件发送失败，请检查邮箱地址是否有效";
        }
    }

    @Override
    public String validateAndRegister(String username, String password, String email, String code, String sessionId) {
        //生成Redis中的key，带上false表明这是注册时发送的验证码
        String key = "email:" + sessionId + ":" + email + ":false";
        //判断Redis是否有这个key
        //有这个key，说明验证码发送过了，才能通过key找验证码进行校验
        if(Boolean.TRUE.equals(stringRedisTemplate.hasKey(key))){
            //从redis中找到对应key的验证码
            String s = stringRedisTemplate.opsForValue().get(key);
            if(s == null) return "验证码失效，请重新请求";//特殊情况：刚找到，验证码就过期了
            //验证码填写正确
            if(s.equals(code)){
                //如果用户名被注册过了，也不能成功注册
                Account account = userMapper.findAccountByNameOrEmail(username);
                if(account != null)return "此用户名已被注册，请更换用户名";
                //所有校验都通过后，才可以注册
                password = encoder.encode(password);//密码需要加密后存储在数据库
                stringRedisTemplate.delete(key);//验证码用完后就要清除掉
                if(userMapper.createAccount(username, password, email) > 0){//插入数据库成功
                    return null;
                }else{//插入数据库失败
                    return "内部错误，请联系管理员";
                }
            }
            //验证码填写错误
            else{
                return "验证码错误，请检查后再提交";
            }
        }
        //Redis中没有对应的key，说明没有发送验证码
        else{
            return "请先请求一封验证码邮件";
        }
    }

    @Override
    public String validateOnly(String email, String code, String sessionId) {
        //生成Redis中的key，带上true表明这是重置密码时发送的验证码
        String key = "email:" + sessionId + ":" + email + ":true";
        //判断Redis是否有这个key
        //有这个key，说明验证码发送过了，才能通过key找验证码进行校验
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(key))) {
            //从redis中找到对应key的验证码
            String s = stringRedisTemplate.opsForValue().get(key);
            if (s == null) return "验证码失效，请重新请求";//特殊情况：刚找到，验证码就过期了
            //验证码填写正确
            if (s.equals(code)) {
                stringRedisTemplate.delete(key);//验证码用完后就要清除掉
                return null;
            }
            //验证码填写错误
            else {
                return "验证码错误，请检查后再提交";
            }
        }
        //Redis中没有对应的key，说明没有发送验证码
        else{
            return "请先请求一封验证码邮件";
        }
    }
    @Override
    public boolean resetPassword(String password, String email) {
        password = encoder.encode(password);//密码加密存储在数据库
        return userMapper.resetPasswordByEmail(password, email) > 0;
    }

}
