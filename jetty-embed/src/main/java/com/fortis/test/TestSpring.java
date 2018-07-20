package com.fortis.test;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.context.WebApplicationContext;

public class TestSpring {
    public static void main(String[] args) {
        ClassPathXmlApplicationContext c = new ClassPathXmlApplicationContext("classpath:org/eclipse/jetty/xml/*.xsd");
        Object d = c.getBean("testController");
        System.out.println();
    }
}
