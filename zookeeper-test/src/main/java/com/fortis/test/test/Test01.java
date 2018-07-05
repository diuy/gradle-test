package com.fortis.test.test;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.List;

public class Test01 {
    public static void main(String[] args) throws Exception {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);//刚开始重试间隔为1秒，之后重试间隔逐渐增加，最多重试不超过三次
        /*RetryPolicy retryPolicy1 = new RetryNTimes(3, 1000);//最大重试次数，和两次重试间隔时间
        RetryPolicy retryPolicy2 = new RetryUntilElapsed(5000, 1000);//会一直重试直到达到规定时间，第一个参数整个重试不能超过时间，第二个参数重试间隔
        //第一种方式
        CuratorFramework client = CuratorFrameworkFactory.newClient("192.168.0.3:2181", 5000,5000,retryPolicy);//最后一个参数重试策略
        */

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
        try {
            client.getChildren().usingWatcher(new CuratorWatcher() {
                @Override
                public void process(WatchedEvent event) throws Exception {
                    System.out.println("###########");
                    System.out.println(event.toString());
                    System.out.println("###########");
                }
            }).forPath("/curator");
        }catch (KeeperException.NoNodeException e){
            System.out.println("no /curator");
        }



        while (testRun()){

            String path = client.create().creatingParentsIfNeeded()//若创建节点的父节点不存在会先创建父节点再创建子节点
                    .withMode(CreateMode.EPHEMERAL)//withMode节点类型，
                    .forPath("/curator/3", "131".getBytes());
            System.out.println(path);

            List<String> list = client.getChildren().forPath("/");
            //System.out.println(list);

            //String re = new String(client1.getData().forPath("/curator/3"));//只获取数据内容
            Stat stat = new Stat();
            String re = new String(client.getData().storingStatIn(stat)//在获取节点内容的同时把状态信息存入Stat对象
                    .forPath("/curator/3"));
           // System.out.println(re);
            //System.out.println(stat);


            client.delete().guaranteed()//保障机制，若未删除成功，只要会话有效会在后台一直尝试删除
                    .deletingChildrenIfNeeded()//若当前节点包含子节点
                    .withVersion(-1)//指定版本号
                    .forPath("/curator");
        }

       // Thread.sleep(Integer.MAX_VALUE);
    }

    private static boolean testRun() throws IOException {
        byte [] value  = new byte[100];
        System.in.read(value);
        return value[0]!='e';
    }

}
