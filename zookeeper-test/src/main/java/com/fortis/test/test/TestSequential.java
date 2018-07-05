package com.fortis.test.test;

import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;

import java.util.List;

public class TestSequential {
    public static void main(String[] args) throws Exception {
        CuratorFramework client = Connector.newClient();

        client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT_SEQUENTIAL).forPath("/si/x");
        client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT_SEQUENTIAL).forPath("/si/y");
        List<String> list = client.getChildren().forPath("/si");
        System.out.println(list);
        client.delete().deletingChildrenIfNeeded().forPath("/si");
        //System.in.read();
    }
}
