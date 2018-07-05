package test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TestQueue {
    public static void main(String[] args){
        ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<>();
        queue.offer("a");
        queue.offer("b");
        queue.offer("c");
        System.out.println(queue.size());

        List<String> aa = new ArrayList<>();
        aa.add("test");

    }
}