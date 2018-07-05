package com.fortis.test.test;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Connector {
    private final static Logger logger = LoggerFactory.getLogger(Connector.class);

    public static CuratorFramework newClient(){

        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 4);//刚开始重试间隔为1秒，之后重试间隔逐渐增加，最多重试不超过三次

        //第二种方式
        CuratorFramework client = CuratorFrameworkFactory.builder().namespace("xc")
                .connectString("172.20.11.115:2181,172.20.11.114:2181,172.20.11.116:2181")
                .sessionTimeoutMs(10000)//会话超时时间
                .connectionTimeoutMs(5000)//连接超时时间
                .retryPolicy(retryPolicy)
                .build();
        client.getConnectionStateListenable().addListener(new ConnectionStateListener() {
            @Override
            public void stateChanged(CuratorFramework client, ConnectionState newState) {
                logger.warn("state:"+client.getZookeeperClient().getCurrentConnectionString()+","+newState);
            }
        });
        client.start();
        return client;
    }
}
