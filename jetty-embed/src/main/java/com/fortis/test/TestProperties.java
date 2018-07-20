package com.fortis.test;

import java.io.IOException;
import java.util.Properties;

public class TestProperties {
    public static void main(String[] args) throws IOException {
        Properties p =new Properties();
        p.load(TestProperties.class.getClassLoader().getResourceAsStream("jetty.properties"));
        System.out.println(p.getProperty("xc"));
        System.out.println(System.getProperties().getProperty("xc"));

    }
}