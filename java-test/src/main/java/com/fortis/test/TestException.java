package com.fortis.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestException {

    private final static Logger logger = LoggerFactory.getLogger(TestException.class);

    public static void main(String[] args) {
        try{
            System.out.println(getString("1,2"));
        }catch (Exception e){
            logger.error("111",e);
            // e.printStackTrace();
        }
    }

    public static int getString(String str) {
        if (str == null || str.length() < 2) {
            throw new IllegalStateException("string is null");
        }
        MyService myService = new MyService();
        try {
            return myService.getValue(str.charAt(0), str.charAt(2));
        }catch (Exception e){
            //e.printStackTrace();
            throw new IllegalStateException("get value error",e);
        }
    }
}
