package com.fortis.test;


import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SpringMvc3 {
    public static void main(String[] args) throws Exception {
        ClassPathXmlApplicationContext c = new ClassPathXmlApplicationContext("classpath:spring-web.xml");
        c.start();
        System.in.read();
        c.stop();
        c.close();
    }
}
