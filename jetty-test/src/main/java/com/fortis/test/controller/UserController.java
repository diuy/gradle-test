package com.fortis.test.controller;

import com.fortis.test.bean.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
    @Autowired
    private User user;

    @RequestMapping("/test")
    public String test() {
        return user.getName() + "->" + user.getAge();
    }
    @RequestMapping("/user")
    public User getUser(){
        return user;
    }


    public UserController(){
        System.out.println("######create TestController");
    }
}
