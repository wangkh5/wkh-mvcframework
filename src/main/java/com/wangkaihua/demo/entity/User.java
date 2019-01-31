package com.wangkaihua.demo.entity;

import java.io.Serializable;

/**
 * @desciption: TODO
 * @author: wangkaihua
 * @date: 2019/1/29 21:18
 */
public class User implements Serializable {

    private String name;
    private int age;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", age=" + age +
                '}';
    }
}
