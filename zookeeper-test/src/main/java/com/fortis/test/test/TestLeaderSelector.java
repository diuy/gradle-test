package com.fortis.test.test;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListener;
import org.apache.curator.framework.state.ConnectionState;

public class TestLeaderSelector {
    static LeaderSelector leaderSelector;

    public static void main(String[] args) throws Exception {
        CuratorFramework client = Connector.newClient();
        leaderSelector = new LeaderSelector(client, "/test_leader", new LeaderSelectorListener() {
            @Override
            public void takeLeadership(CuratorFramework client) throws Exception {
                System.out.println("***** got start");
                try {
                    Thread.sleep(200000000);
                } catch (InterruptedException ignore) {
                    System.out.println("***** got over");
                }
            }

            @Override
            public void stateChanged(CuratorFramework client, ConnectionState newState) {
                System.out.println("***** state:" + newState);
                if(!newState.isConnected())
                    leaderSelector.interruptLeadership();
            }
        });
        leaderSelector.autoRequeue();
        leaderSelector.start();
        System.in.read();
    }
}
