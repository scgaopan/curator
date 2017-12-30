package com.arthur.zookeeper.barrier;

import com.arthur.zookeeper.CuratorClinetUtil;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.barriers.DistributedDoubleBarrier;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * DistributedDoubleBarrier
 * Created by gaopan on 17/12/25.
 */
public class DistributedDoubleBarrierExample {

    private static final int QTY = 5;
    private static final String PATH = "/examples/barrier";

    public static void main(String[] args) throws Exception {
        try {
            CuratorFramework client = CuratorClinetUtil.getConnection();

            ExecutorService service = Executors.newFixedThreadPool(QTY);
            for (int i = 0; i < QTY; ++i) {
                final DistributedDoubleBarrier barrier = new DistributedDoubleBarrier(client, PATH, QTY);
                final int index = i;
                Callable<Void> task = new Callable<Void>() {
                    public Void call() throws Exception {

                        Thread.sleep((long) (3 * Math.random()));
                        System.out.println("Client #" + index + " enters");
                        barrier.enter();
                        System.out.println("Client #" + index + " begins");
                        Thread.sleep((long) (3000 * Math.random()));
                        barrier.leave();
                        System.out.println("Client #" + index + " left");
                        return null;
                    }
                };
                service.submit(task);
            }


            service.shutdown();
            service.awaitTermination(10, TimeUnit.MINUTES);

        }finally {

        }

    }
}
