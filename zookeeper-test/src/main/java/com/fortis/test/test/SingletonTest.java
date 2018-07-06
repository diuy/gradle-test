package com.fortis.test.test;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.framework.recipes.locks.RevocationListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SingletonTest {
    private final static Logger logger = LoggerFactory.getLogger(SingletonTest.class);

    public static void  main(String []args){

        CuratorFramework client = Connector.newClient();

        InterProcessMutex mutex = new InterProcessMutex(client,"/SingletonTest");
        try {
            logger.info("********* acquire start");
            mutex.acquire();
            mutex.makeRevocable(new RevocationListener<InterProcessMutex>() {
                @Override
                public void revocationRequested(InterProcessMutex forLock) {
                    logger.info("********* revocationRequested");
                }
            });
            logger.info("********* acquire got");
            System.in.read();
            //Thread.sleep(10000);
            mutex.release();
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }
    }

}
