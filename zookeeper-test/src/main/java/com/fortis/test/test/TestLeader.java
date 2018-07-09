package com.fortis.test.test;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

/**
 * 一旦有新的状态变化，或网络变化就会先停止服务再次尝试创建
 */
public class TestLeader {
    private static CuratorFramework client;
    private final static Logger logger = LoggerFactory.getLogger(TestLeader.class);
    private final static String path = "/tl";
    private static byte[] data;
    private static boolean started = false;
    private static volatile boolean closed = false;
    private static volatile CountDownLatch downLatch;

    public static void main(String[] args) throws IOException, InterruptedException {
        data = UUID.randomUUID().toString().getBytes();
        client = Connector.newClient();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!closed) {
                    acquire();
                    if (closed)
                        break;

                    start();
                    if (closed) {
                        stop();
                        break;
                    }

                    check();
                    stop();
                }

                try {
                    client.delete().guaranteed().forPath(path);
                } catch (KeeperException.NoNodeException ignore) {
                } catch (Exception e) {
                    logger.warn("delete path error", e);
                }
            }
        });
        thread.start();
        System.in.read();
        closed = true;
        CountDownLatch latch = downLatch;
        if (latch != null)
            latch.countDown();
        thread.join();
    }

    private static void acquire() {
        while (!closed) {
            try {
                client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path, data);
                logger.info("acquire: create success");
                break;
            } catch (KeeperException.NodeExistsException ignore) {
                logger.info("acquire: node existed");
                try {
                    CountDownLatch latch = new CountDownLatch(1);
                    byte[] d = client.getData().usingWatcher(new CuratorWatcher() {
                        @Override
                        public void process(WatchedEvent event) throws Exception {
                            latch.countDown();
                        }
                    }).forPath(path);
                    if (!Arrays.equals(d, data)) {
                        logger.info("acquire: data is not created by myself,start wait");
                        downLatch = latch;
                        if (closed)
                            continue;
                        latch.await();
                    } else {
                        logger.info("acquire: data is created by myself");
                        break;
                    }
                } catch (KeeperException.NoNodeException ignore1) {
                    logger.info("acquire: node not existed");
                    //continue;
                } catch (Exception e) {
                    logger.warn("acquire: getData error", e);
                }
            } catch (Exception e) {
                logger.warn("acquire: create error", e);
            }
        }
    }

    private static void check() {
        try {
            CountDownLatch latch = new CountDownLatch(1);
            byte[] d = client.getData().usingWatcher(new CuratorWatcher() {
                @Override
                public void process(WatchedEvent event) throws Exception {
                    latch.countDown();
                }
            }).forPath(path);
            if (!Arrays.equals(d, data)) {
                logger.info("acquire: data is not created by myself");
                return;
            } else {
                logger.info("check: data is created by myself,start wait");
                downLatch = latch;
                if (!closed)
                    latch.await();
            }
        } catch (KeeperException.NoNodeException ignore1) {
            logger.info("check: node not existed");
            return;
        } catch (Exception e) {
            logger.warn("check: getData error", e);
            return;
        }
    }

    private static void start() {
        if (started)
            return;

        started = true;
        logger.info("started");
    }

    private static void stop() {
        if (!started)
            return;

        started = false;
        logger.info("stopped");
    }

}
