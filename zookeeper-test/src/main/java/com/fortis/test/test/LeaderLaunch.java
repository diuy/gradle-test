package com.fortis.test.test;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.curator.utils.PathUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 分布式单例运行工具类，只有在获得锁的线程才会启动服务
 */
public class LeaderLaunch implements Runnable {
    private final static Logger logger = LoggerFactory.getLogger(LeaderLaunch.class);
    private final CuratorFramework client;
    private final String path;
    private final Launcher launcher;
    private final long waitTime;

    private byte[] data;
    private Thread thread;

    private volatile boolean closed = false;
    private volatile CountDownLatch downLatch;
    private boolean launchStarted = false;


    /**
     * 创建
     *
     * @param client   zookeeper客户端
     * @param launcher 服务执行器
     * @param path     zookeeper节点路径
     * @param waitTime 争取leader的时间间隔
     */
    public LeaderLaunch(CuratorFramework client, Launcher launcher, String path, long waitTime) {
        if (client == null)
            throw new NullPointerException("client");
        if (launcher == null)
            throw new NullPointerException("launcher");
        if (path == null || path.length() == 0)
            throw new NullPointerException("path");

        this.client = client;
        this.launcher = launcher;
        this.path = PathUtils.validatePath(path);
        if (waitTime < 10 * 1000)
            this.waitTime = 10 * 1000;
        else
            this.waitTime = waitTime;
    }

    public void start() {
        if (thread != null)
            return;

        data = UUID.randomUUID().toString().getBytes();
        thread = new Thread(this);
        thread.start();
    }

    public void stop() {
        if (closed)
            return;

        closed = true;
        CountDownLatch latch = downLatch;
        if (latch != null)
            latch.countDown();
        try {
            thread.join(20 * 1000);
        } catch (InterruptedException ignore) {
        }
    }

    @Override
    public void run() {
        while (!closed) {
            acquire();
            if (closed)
                break;
            checkData();
        }
        if (launchStarted) {
            stopLauncher();
            try {
                client.delete().guaranteed().forPath(path);
            } catch (KeeperException.NoNodeException ignore) {
            } catch (Exception e) {
                logger.warn("delete path error", e);
            }
        }

    }

    private void acquire() {
        while (!closed) {
            try {
                client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path, data);
                logger.info("acquire: create success");
                startLauncher();//创建成功，启动
                break;
            } catch (KeeperException.NodeExistsException ignore) {
                logger.info("acquire: node existed");
                try {
                    final CountDownLatch latch = new CountDownLatch(1);
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
                        stopLauncher();//确认不是自己创建的，停止当前在运行的
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
                        startLauncher();//已经是自己创建的，再次启动
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
     */
    @SuppressWarnings("All")
    private void checkData() {
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

    private void startLauncher() {
        if (launchStarted)
            return;

        launchStarted = true;
        logger.info("started");
        if (launcher != null) {
            launcher.start();
        }
    }

    private void stopLauncher() {
        if (!launchStarted)
            return;

        launchStarted = false;
        logger.info("stopped");
        if (launcher != null) {
            launcher.stop();
        }
    }


    public interface Launcher {
        void start();

        void stop();
    }
}
