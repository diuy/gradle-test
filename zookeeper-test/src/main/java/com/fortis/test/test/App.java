package com.fortis.test.test;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;

public class App {
    private static CuratorFramework client;
    private final static Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) throws Exception {
        client = Connector.newClient();
        watch("/1");

        System.in.read();
    }

    private static String bytesToString(byte[] s){
        if(s==null)
            return null;
        if(s.length==0)
            return "";
        try {
            return new String(s,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }

    private static void watch(final String path){
        try {
            Stat s = client.checkExists().usingWatcher(new CuratorWatcher() {
                @Override
                public void process(WatchedEvent event) throws Exception {
                    System.out.println("event:"+event);
                    watch(path);
                }
            }).forPath("/1");
            System.out.println("stat:"+s);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private static void watch2(final String path){
        try {
            byte[] s = client.getData().usingWatcher(new CuratorWatcher() {
                @Override
                public void process(WatchedEvent event) throws Exception {
                    logger.info("########## event:"+event);
                    watch(path);
                }
            }).forPath(path);
            logger.info("########## data:"+bytesToString(s));
        }catch (Exception e){
            logger.info("########## error:",e);
        }

    }
}
