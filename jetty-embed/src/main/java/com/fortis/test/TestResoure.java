package com.fortis.test;

import org.eclipse.jetty.util.resource.Resource;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class TestResoure {
    public static void main(String[] args) throws IOException {
        Resource ddd = Resource.newSystemResource("about.html");
        URL sssss = TestResoure.class.getClassLoader().getResource("about.html");

        List<String> list = new ArrayList<>();
        list.add("1");
        list.add("2");
        list.add("3");

        ListIterator<String> iterator = list.listIterator(3);
        while (iterator.hasPrevious()){
            System.out.println(iterator.previous());;
        }

        System.out.println(ddd);
    }
}
