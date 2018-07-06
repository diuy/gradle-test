package com.fortis.test.test;

import org.apache.curator.framework.CuratorFramework;

public class TestProtection {
    public static void main(String []args) throws Exception {
        CuratorFramework client = Connector.newClient();
        client.create().withProtection().forPath("/hx");
        System.out.println(client.getChildren().forPath("/"));
    }
}
