package com.fortis.test.test;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.BackgroundCallback;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.List;

public class Test02 {
    public static void main(String[] args) throws Exception {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 4);//刚开始重试间隔为1秒，之后重试间隔逐渐增加，最多重试不超过三次

        //第二种方式
        CuratorFramework client = CuratorFrameworkFactory.builder().namespace("xc")
                .connectString("172.20.11.115:2181,172.20.11.114:2181,172.20.11.116:2181")
                .sessionTimeoutMs(5000)//会话超时时间
                .connectionTimeoutMs(5000)//连接超时时间
                .retryPolicy(retryPolicy)
                .build();
        client.getConnectionStateListenable().addListener(new ConnectionStateListener() {
            @Override
            public void stateChanged(CuratorFramework client, ConnectionState newState) {
                System.out.println("state:"+client.getZookeeperClient().getCurrentConnectionString()+","+newState);
            }
        });
        client.start();
//        try {
//            client.getChildren().usingWatcher(new CuratorWatcher() {
//                @Override
//                public void process(WatchedEvent event) throws Exception {
//                    System.out.println(event.toString());
//                }
//            }).forPath("/");
//        }catch (KeeperException.NoNodeException e){
//            System.out.println("no /curator");
//        }



        String path = client.create()//.creatingParentsIfNeeded()//若创建节点的父节点不存在会先创建父节点再创建子节点
                .withMode(CreateMode.PERSISTENT)//withMode节点类型，
                .inBackground(new BackgroundCallback() {
                    @Override
                    public void processResult(CuratorFramework client, CuratorEvent event) throws Exception {
                        System.out.println("back:\n"+event.toString());
                    }
                })
                .forPath("/curator", "131".getBytes());
        System.out.println(path);

        Stat s = client.checkExists().usingWatcher(new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                System.out.println("checkExists:"+event.toString());
            }
        }).forPath("/a/b");


        try {
            List<String> ss = client.getChildren().usingWatcher(new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    System.out.println("getChildren:"+event.toString());
                }
            }).forPath("/c");
            System.out.println("3:"+ss);

        }catch (KeeperException.NoNodeException e){
            System.out.println("getChildren:"+"no node");
        }


        try {
            byte[] d = client.getData().usingWatcher(new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    System.out.println("getData:"+event.toString());
                }
            }).forPath("/xx");

            System.out.println("getData:"+d);
        }catch (KeeperException.NoNodeException e){
            System.out.println("getData:"+"no node");
        }


        System.in.read();
       // Thread.sleep(Integer.MAX_VALUE);
    }

    private static boolean testRun() throws IOException {
        byte [] value  = new byte[100];
        System.in.read(value);
        return value[0]!='e';
    }

}
