package com.fortis.test.service;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

@Service
public class TestServer implements InitializingBean, DisposableBean{
    @Override
    public void destroy() throws Exception {
        System.out.println("TestServer destroy");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("TestServer afterPropertiesSet");
    }
}
