package com.fortis.test.controller;

import com.fortis.test.bean.User;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController implements InitializingBean, DisposableBean, ApplicationContextAware {
    private ApplicationContext applicationContext;

    @Autowired
    private User user;

    @RequestMapping("/test")
    public String test() {
        return user.getName() + "->" + user.getAge();
    }

    @RequestMapping("/user")
    public User getUser() {
        return user;
    }


    public UserController() {
        System.out.println("######create TestController");
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void destroy() throws Exception {
        System.out.println("UserController destroy");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("UserController afterPropertiesSet");
    }
}
