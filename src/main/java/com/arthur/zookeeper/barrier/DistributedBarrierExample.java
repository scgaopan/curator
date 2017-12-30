package com.arthur.zookeeper.barrier;

import com.arthur.zookeeper.CuratorClinetUtil;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.barriers.DistributedBarrier;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 *
 * DistributedBarrier
 *
 * Created by gaopan on 17/12/25.
 */
public class DistributedBarrierExample {
    private static final int QTY=5;
    private  static final String PATH="/examples/barrier";

    public static void main(String[] args) throws Exception {

        try {
            CuratorFramework curatorFramework= CuratorClinetUtil.getConnection();
            ExecutorService service= Executors.newFixedThreadPool(QTY);
            DistributedBarrier distributedBarrier=new DistributedBarrier(curatorFramework,PATH);
            //首先你需要设置栅栏，它将阻塞在它上面等待的线程
            distributedBarrier.setBarrier();


            for (int i = 0; i <QTY ; i++) {
                final DistributedBarrier barrier=new DistributedBarrier(curatorFramework,PATH);
                final  int index=i;
                Callable<Void> callable=new Callable<Void>() {
                    public Void call() throws Exception {
                        Thread.sleep((long) (3 * Math.random()));
                        System.out.println("Client #" + index + " waits on Barrier");
                        //然后需要阻塞的线程调用方法 等待放行条件   线程会阻塞在这儿，等待  distributedBarrier.removeBarrier();
                        barrier.waitOnBarrier();

                        System.out.println("Client #" + index + " begins working");
                        return null;
                    }
                };
                service.submit(callable);

            }
            service.shutdown();


            Thread.sleep(5000);//主线程等待
            System.out.println("all Barrier instances should wait the condition");
            //当条件满足时，移除栅栏，所有等待的线程将继续执行
            distributedBarrier.removeBarrier();

            service.awaitTermination(10, TimeUnit.MINUTES);
            //Thread.sleep(8000);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
