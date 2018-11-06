package com.fortis.test;

public class MyService {

    public int getValue(int x,int y){
        if(x<y)
            throw new IllegalStateException("x<y");

        return x-y;
    }
}
