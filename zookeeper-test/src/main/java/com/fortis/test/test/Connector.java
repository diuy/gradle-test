package com.fortis.test.test;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.ExponentialBackoffRetry;

public class Connector {
    public static CuratorFramework newClient(){
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
        return client;
    }
}
