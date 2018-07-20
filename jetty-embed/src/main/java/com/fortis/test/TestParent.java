package com.fortis.test;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.web.context.WebApplicationContext;

public class TestParent {
    public static void main(String[] args) {
        ClassPathXmlApplicationContext c = new ClassPathXmlApplicationContext("classpath:spring.xml");
        c.getEnvironment().setActiveProfiles();
        WebApplicationContext webApplicationContext = c.getBean("app",WebApplicationContext.class);
        ApplicationContext p = webApplicationContext.getParent();
        System.out.printf(""+p);
    }
}
