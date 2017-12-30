package com.arthur.zookeeper;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.junit.Test;

/**
 * Created by gaopan on 17/12/23.
 */
public class CuratorClinetUtil {

    /**
     * 获取连接
     * @return
     */
    public static CuratorFramework getConnection() {
        CuratorFramework client;
        client = CuratorFrameworkFactory.newClient("192.192.16.58:2181",
                new ExponentialBackoffRetry(1000, 3))//
        ;
        client.start();
        return client;
    }

    /**
     * 关闭连接
     * @param client
     */
    public static  void close(CuratorFramework client){
        if (client!=null){
            client.close();
        }

    }

    /**
     * Master选举
     */
    @Test
    public  void selector4LeaderSelector(){
        try {
            CuratorFramework curatorFramework= CuratorClinetUtil.getConnection();
            LeaderSelectorListenerAdapter leaderSelectorListenerAdapter=new LeaderSelectorListenerAdapter() {
                @Override
                public void takeLeadership(CuratorFramework curatorFramework) throws Exception {
                    System.out.println("成为了master角色，当前TreadName="+Thread.currentThread().getName());
                    Thread.sleep(1000);
                    System.out.println("释放");
                }
            };
            LeaderSelector selector=new LeaderSelector(curatorFramework,"/selector",leaderSelectorListenerAdapter);

            selector.autoRequeue();//就是自动抢
            selector.start();
            Thread.sleep(Integer.MAX_VALUE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
