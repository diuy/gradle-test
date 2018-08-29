package com.fortis.test;

import java.util.ArrayList;
import java.util.List;

//-Xms20M -Xmx20M -Xmn10M -XX:+PrintGCDetails -XX:SurvivorRatio=8 -XX:+HeapDumpOnOutOfMemoryError
public class HeapOOM {
    private long id;
    private long id2;
    private long id3;
    private long id4;
    private long id5;

    public static void main(String[] args) {
        List<HeapOOM> list = new ArrayList<>();
        for (;;){
            list.add(new HeapOOM());
        }
    }
}
