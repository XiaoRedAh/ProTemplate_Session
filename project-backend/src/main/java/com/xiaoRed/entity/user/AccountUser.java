package com.xiaoRed.entity.user;

import lombok.Data;

/**
 * Account是数据库直接映射的实体类
 * 而这个AccountUser是存放用户详细信息的，根据不同项目的需求可以进行扩充
 */
@Data
public class AccountUser {
    long id;
    String username;
    String email;
}
