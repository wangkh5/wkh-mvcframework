package com.wangkaihua.demo.action;

import com.wangkaihua.demo.entity.User;
import com.wangkaihua.demo.service.UserService;
import com.wangkaihua.mvcframework.annotation.WKHAutowired;
import com.wangkaihua.mvcframework.annotation.WKHController;
import com.wangkaihua.mvcframework.annotation.WKHRequestMappring;
import com.wangkaihua.mvcframework.annotation.WKHRequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @desciption: 用户Action
 * @author: wangkaihua
 * @date: 2019/1/19 21:40
 */
@WKHController
@WKHRequestMappring("/user")
public class UserAction {

    @WKHAutowired
    private UserService userService;

    @WKHRequestMappring("/get")
    public void getUser(HttpServletRequest request, HttpServletResponse response,
                        @WKHRequestParam("name") String name) {
        User user = userService.getUser(name);
        try {
             response.getWriter().write(user.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @WKHRequestMappring("/add")
    public void addUser(HttpServletRequest request, HttpServletResponse response,
                        @WKHRequestParam("name")String name,
                        @WKHRequestParam("age") Integer age) {
        User user = userService.addUser(name, age);
        try {
            response.getWriter().write(user.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
