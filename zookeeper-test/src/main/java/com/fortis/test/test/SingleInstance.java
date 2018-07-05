package com.fortis.test.test;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class SingleInstance {
    private final static Logger logger = LoggerFactory.getLogger(SingleInstance.class);

    public static void main(String[] args) throws Exception {
        final CuratorFramework client = Connector.newClient();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    join(client);
                    logger.info(ManagementFactory.getRuntimeMXBean().getName() + ":started");
                } catch (InterruptedException e) {
                    logger.error("join",e);
                }
            }
        }).start();

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

        String line;
        while ((line = in.readLine()) != null) {
            line = line.trim();
            if ("r".equals(line)) {
                List<String> list = client.getChildren().forPath("/");
                logger.info("getChildren:"+list);
            }else if("e".equals(line)){
                logger.info("exit");
            }
        }
    }

    private static void join(CuratorFramework client) throws InterruptedException {
        final String path = "/single_instance";
        for (; ; ) {
            try {
                client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path);
                logger.info("create success");
                return;
            } catch (KeeperException.NodeExistsException ignore) {
                try {
                    CountDownLatch latch = new CountDownLatch(1);
                    CuratorWatcher watcher = new CuratorWatcher() {
                        @Override
                        public void process(WatchedEvent event) throws Exception {
                            //  任何事件发生都可以尝试重新创建,包括数据改变
                            // （这时是不能创建成功的，但是可以简化程序结构，重新创建监听）
                            latch.countDown();
                        }
                    };
                    Stat stat = client.checkExists().usingWatcher(watcher).forPath(path);
                    if (stat != null) {
                        logger.info("wait delete");
                        latch.await();
                    }
                } catch (InterruptedException e) {
                    throw e;
                } catch (Exception e) {
                    Thread.sleep(5000);
                    logger.info("checkExists error:" + e.getMessage());
                }
            } catch (Exception e) {
                logger.info("create error:" + e.getMessage());
                Thread.sleep(5000);
            }
        }
    }
}
