package com.fortis.test.bean;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

public class User implements InitializingBean, DisposableBean {
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

    public User(){
        System.out.println("######create User");
    }

    @Override
    public void destroy() throws Exception {
        System.out.println("User destroy");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("User afterPropertiesSet");
    }
}
