package com.wangkaihua.demo.service.impl;

import com.wangkaihua.demo.entity.User;
import com.wangkaihua.demo.service.UserService;
import com.wangkaihua.mvcframework.annotation.WKHService;

/**
 * @desciption: 用户业务
 * @author: wangkaihua
 * @date: 2019/1/22 14:58
 */
@WKHService
public class UserServiceImpl implements UserService {


    @Override
    public User getUser(String name) {
        User user = new User();
        user.setName(name);
        user.setAge(24);
        return user;
    }

    @Override
    public User addUser(String name, int age) {
        User user = new User();
        user.setName(name);
        user.setAge(age);
        return user;
    }
}
