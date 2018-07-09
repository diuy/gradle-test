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
import java.util.concurrent.TimeUnit;

/**
 * 创建成功后确认不能再次创建后 停止
 */
public class TestLeader2 {
    private static CuratorFramework client;
    private final static Logger logger = LoggerFactory.getLogger(TestLeader2.class);
    private final static String path = "/tl";
    private static byte[] data;
    private static boolean started = false;
    private static volatile boolean closed = false;
    private static volatile CountDownLatch downLatch;

    //@SuppressWarnings("All")
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
                    checkData();
                }
                stop();

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

   // @SuppressWarnings("All")
    private static void acquire() {
        while (!closed) {
            try {
                client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path, data);
                logger.info("acquire: create success");
                start();//创建成功，启动
                break;
            } catch (KeeperException.NodeExistsException ignore) {
                logger.info("acquire: node existed");
                try {
                    final CountDownLatch  latch = new CountDownLatch(1);
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
                        stop();//确认不是自己创建的，停止当前在运行的
                        latch.await();
                        logger.info("acquire: data state changed");
                        //在等到了节点事件变化后，（网络变化，节点消失，数据修改等），先等待10秒再尝试创建，
                        //给一段时间给Leader尝试回复
                        CountDownLatch latch2 = new CountDownLatch(1);
                        downLatch = latch2;
                        if (closed)
                            continue;
                        latch2.await(10, TimeUnit.SECONDS);
                    } else {
                        start();//已经是自己创建的，再次启动
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

    /**
     * 确认无法继续保持Leader返回 false
     * 可以再次尝试获得Leader返回 true
     *
     */
    // @SuppressWarnings("All")
    private static void checkData() {
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
                //return;
            } else {
                logger.info("check: data is created by myself,start wait");
                downLatch = latch;
                if (!closed)
                    latch.await();
                // return;
            }
        } catch (KeeperException.NoNodeException ignore1) {
            logger.info("check: node not existed");
            // return;
        } catch (Exception e) {
            logger.warn("check: getData error", e);
            //  return;
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
