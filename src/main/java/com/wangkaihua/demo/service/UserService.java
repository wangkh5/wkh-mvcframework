package com.wangkaihua.demo.service;

import com.wangkaihua.demo.entity.User;

/**
 * @desciption: TODO
 * @author: wangkaihua
 * @date: 2019/1/19 21:41
 */
public interface UserService {

    User getUser(String name);

    User addUser(String name, int age);
}
