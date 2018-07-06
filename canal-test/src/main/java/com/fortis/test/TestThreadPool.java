package test;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TestThreadPool{
    public static void main(String []args){
        ThreadPoolExecutor executor = new ThreadPoolExecutor(10, 10,
                0L, TimeUnit.MILLISECONDS,
                new SynchronousQueue<Runnable>());
        executor.execute(new Runnable() {
            @Override
            public void run() {
//                try {
//                    Thread.sleep(3000);
//                    System.out.println("over");
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
            }
        });
        executor.shutdown();
    }
}


