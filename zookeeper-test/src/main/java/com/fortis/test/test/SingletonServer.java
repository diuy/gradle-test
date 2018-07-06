package com.fortis.test.test;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.framework.api.CuratorListener;
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
import java.util.concurrent.LinkedBlockingQueue;

//SingletonServer
public class SingletonServer implements Runnable {
    private final static Logger logger = LoggerFactory.getLogger(SingletonServer.class);
    private final String path;
    private final Runnable startRunnable;
    private final Runnable stopRunnable;
    private final CuratorFramework client;
    private Thread thread;

    private SingletonServer(CuratorFramework client, String path, Runnable startRunnable, Runnable stopRunnable) {
        this.client = client;
        this.path = path;
        this.startRunnable = startRunnable;
        this.stopRunnable = stopRunnable;
    }

    public void start() {
        thread = new Thread(this);
        thread.start();
    }

    public void stop() {

    }

    public static void main(String[] args) throws Exception {
        final CuratorFramework client = Connector.newClient();
        new Thread(new Runnable() {
            @Override
            public void run() {
//                try {
//                    acquire();
//                    logger.info(ManagementFactory.getRuntimeMXBean().getName() + ":started");
//                } catch (InterruptedException e) {
//                    logger.error("join", e);
//                }
            }
        }).start();

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

        String line;
        while ((line = in.readLine()) != null) {
            try {
                line = line.trim();
                if ("r".equals(line)) {
                    List<String> list = client.getChildren().forPath("/");
                    logger.info("getChildren:" + list);
                } else if ("e".equals(line)) {
                    logger.info("exit");
                }
            } catch (Exception e) {
                logger.error("getChildren error:", e);
            }
        }
    }

    private void acquire() throws InterruptedException {
        for (; ; ) {
            try {
                client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path);
                logger.info(path + ":create success");
                return;
            } catch (KeeperException.NodeExistsException ignore) {
                try {
                    CountDownLatch latch = new CountDownLatch(1);
                    CuratorWatcher watcher = new CuratorWatcher() {
                        @Override
                        public void process(WatchedEvent event) throws Exception {
                            //  任何事件发生都可以尝试重新创建,包括数据改变,连接断开
                            // （这时是不能创建成功的，但是可以简化程序结构，重新创建监听）
                            latch.countDown();
                        }
                    };
                    Stat stat = client.checkExists().usingWatcher(watcher).forPath(path);
                    if (stat != null) {
                        logger.info(path + ":wait delete");
                        latch.await();
                    }
                } catch (InterruptedException e) {
                    throw e;
                } catch (Exception e) {
                    Thread.sleep(5000);
                    logger.info(path + ":checkExists error:" + e.getMessage());
                }
            } catch (Exception e) {
                logger.info(path + ":create error:" + e.getMessage());
                Thread.sleep(5000);
            }
        }
    }

    @Override
    public void run() {
        try {
            acquire();
        } catch (InterruptedException ignore) {
            logger.info("interrupted");
            return;
        }catch (Exception e){
            logger.error("acquire failed",e);
            return;
        }
        LinkedBlockingQueue<CuratorEvent> queue = new LinkedBlockingQueue<>();
        client.getCuratorListenable().addListener(new CuratorListener() {
            @Override
            public void eventReceived(CuratorFramework client, CuratorEvent event) throws Exception {
                queue.offer(event);
            }
        });
        try {
            CuratorEvent event=queue.take();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
