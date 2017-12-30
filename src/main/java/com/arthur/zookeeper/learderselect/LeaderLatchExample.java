package com.arthur.zookeeper.learderselect;

import com.arthur.zookeeper.CuratorClinetUtil;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.utils.CloseableUtils;
import org.testng.collections.Lists;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**Leader latch方式
 * Created by gaopan on 17/12/24.
 * 首先我们创建了10个LeaderLatch，启动后它们中的一个会被选举为leader。
 * 因为选举会花费一些时间，start后并不能马上就得到leader。 通过hasLeadership查看自己是否是leader，
 * 如果是的话返回true。 可以通过.getLeader().getId()可以得到当前的leader的ID。 只能通过close释放当前的领导权。
 * await是一个阻塞方法， 尝试获取leader地位，但是未必能上位。
 */
public class LeaderLatchExample {

    private static final int CLIENT_QTY = 10;
    private static final String PATH = "/examples/leader";

    public static void main(String[] args) throws  Exception{

        List<CuratorFramework> clients= Lists.newArrayList();
        List<LeaderLatch> examples=Lists.newArrayList();

        try {
            //建10个连接
            for (int i=0;i<CLIENT_QTY;i++){
                CuratorFramework client=CuratorClinetUtil.getConnection();
                clients.add(client);
                LeaderLatch example=new LeaderLatch(client,PATH,"Client #"+i);
                examples.add(example);
                //client.start();
                example.start();
            }
            Thread.sleep(10000);

            LeaderLatch currentleaderLatch=null;//当前领导者

            for (int i = 0; i <CLIENT_QTY ; i++) {
                LeaderLatch example=examples.get(i);
                if(example.hasLeadership()){
                    currentleaderLatch=example;
                }
            }
                System.out.println("current leader is "+currentleaderLatch.getId());
                System.out.println("release the leader " + currentleaderLatch.getId());
                //当前的leader 释放锁
                currentleaderLatch.close();
                examples.get(0).await(2, TimeUnit.SECONDS);//await是一个阻塞方法， 尝试获取leader地位，但是未必能上位。获取leader权，超时时间是2s
                System.out.println("Client #0 maybe is selected as the leader or not although it want to be");
                System.out.println("the new leader is " + examples.get(0).getLeader().getId());//查看当前的领导者

                System.out.println("Press enter/return to quit\n");
                new BufferedReader(new InputStreamReader(System.in)).readLine();

        } finally {
            System.out.println("Shutting down...");
            for (LeaderLatch exampleClient : examples) {
                CloseableUtils.closeQuietly(exampleClient);
            }
            for (CuratorFramework client : clients) {
                CloseableUtils.closeQuietly(client);
            }
        }

    }
}
