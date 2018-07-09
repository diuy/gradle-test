package com.fortis.test.test;

import org.apache.curator.framework.CuratorFramework;

import java.io.IOException;

public class TestLeaderLaunch {
    private static CuratorFramework client;

    public static void main(String[] args) throws IOException {
        client = Connector.newClient();
        LeaderLaunch leaderLaunch = new LeaderLaunch(client, new LeaderLaunch.Launcher() {
            @Override
            public void start() {

            }

            @Override
            public void stop() {

            }
        }, "/0000", 0);
        leaderLaunch.start();

        System.in.read();
        leaderLaunch.stop();
    }
}
