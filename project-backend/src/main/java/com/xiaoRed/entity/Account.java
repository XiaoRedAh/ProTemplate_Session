package com.xiaoRed.entity;

import lombok.Data;

@Data
public class Account {
    long id;
    String username;
    String password;
    String email;
}
