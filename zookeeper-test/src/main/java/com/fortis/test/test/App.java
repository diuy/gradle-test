package com.fortis.test.test;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.data.Stat;

public class App {
    private static CuratorFramework client;
    public static void main(String[] args) throws Exception {
        client = Connector.newClient();
        watch("/1");

        System.in.read();
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
}
