package com.fortis.test;

import com.fortis.test.controller.UserController;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;

public class TestSpring {
    public static void main(String[] args) throws IOException {
        ClassPathXmlApplicationContext c = new ClassPathXmlApplicationContext("classpath:spring.xml");
        ( (UserController)c.getBean("userController")).getUser();
        System.in.read();
    }
}
