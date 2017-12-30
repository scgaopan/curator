package com.arthur.zookeeper;

import com.google.common.annotations.VisibleForTesting;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.BackgroundCallback;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;
import org.apache.curator.utils.CloseableUtils;
import org.apache.zookeeper.data.Stat;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by gaopan on 17/12/21.
 */



public class CuratorClient {



    /**
     * 增加节点
     * 节点类型一共有四种
     CreateMode.EPHEMERAL 临时节点
     CreateMode.EPHEMERAL_SEQUENTIAL 临时顺序节点
     CreateMode.PERSISTENT 持久节点
     CreateMode.PERSISTENT_SEQUENTIAL 持久顺序节点
     * @throws Exception
     */

    @Test
    public void addData (){
        try {
            CuratorFramework client = CuratorClinetUtil.getConnection();
            client.create().forPath("/c1/c4", "nodeeeCahe".getBytes());//创建一个节点
            //client.create().withMode(CreateMode.EPHEMERAL).forPath("/e1","e1".getBytes());
            //creatingParentsIfNeeded 如果父节点不存在 创建之
            //client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath("/p1/p2/p3","p1".getBytes());

            client.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 删除数据
     * @throws Exception
     */
    @Test
    public  void  deleteData() {
        try {
            CuratorFramework client = CuratorClinetUtil.getConnection();
            //递归删除子节点
            client.delete().deletingChildrenIfNeeded().forPath("/c1/c4");

            //强制保证删除
           // client.delete().guaranteed().forPath("/t1");
            client.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 读取数据
     * @throws Exception
     */
    @Test
    public  void readData() {
        try {
            CuratorFramework curatorFramework=CuratorClinetUtil.getConnection();
            byte[] data=curatorFramework.getData().forPath("/t1");
            System.out.println(new String(data));
            //1.读取一个节点同时获取节点的stat
            Stat stat = new Stat();
            byte [] data1= curatorFramework.getData().storingStatIn(stat).forPath("/t1");

            System.out.println(stat);
            //2.获取某个节点的所有子节点路径
            //curatorFramework.getChildren().forPath("/t1");
            curatorFramework.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 更新数据
     */
    @Test
    public   void updateDate(){
        try {
            CuratorFramework curatorFramework=CuratorClinetUtil.getConnection();
            curatorFramework.setData().forPath("/c1/c3","dffeeed".getBytes());

            curatorFramework.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //事务
//        curatorFramework.inTransaction().check().forPath("path")
//                .and()
//                .create().withMode(CreateMode.EPHEMERAL).forPath("path","data".getBytes())
//                .and()
//                .setData().withVersion(10086).forPath("path","data2".getBytes())
//                .and()
//                .commit();


    }


    /**
     * 后台异步执行
     * @throws Exception
     */
    @Test
    public   void  backGrandCallBack() {
        try {
            ExecutorService executorService= Executors.newFixedThreadPool(2);
            final CountDownLatch countDownLatch=new CountDownLatch(1);
            BackgroundCallback backgroundCallback=new BackgroundCallback() {
                public void processResult(CuratorFramework client, CuratorEvent event) throws Exception {

                    System.out.println(event.getType().name());
                    System.out.println("EventCode:" + event.getResultCode());
                    System.out.println(Thread.currentThread().getName());
                    countDownLatch.countDown();
                }
            };
            CuratorFramework client=CuratorClinetUtil.getConnection();

            client.create().inBackground(backgroundCallback,executorService).forPath("/sync");
            countDownLatch.await();
            executorService.shutdown();
           // client.close();
            CloseableUtils.closeQuietly(client);//实现closeable接口的类可以用此方法关闭
            System.out.println("操作完成");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 监听节点数据变化.
     */
    @Test
    public   void nodeCaheLinstner() {
        try {
            CuratorFramework client = CuratorClinetUtil.getConnection();
            final NodeCache cache = new NodeCache(client, "/nodeCahe");
            //NodeCache的start方法传递一个boolean类型参数，默认false,如果设置为true,那么NodeCache在第一次启动打的时候就会立刻在Zookeeper上读取对应节点的数据内容，并保存在Cache中。
            //NodeCache不仅可以用于监听数据节点内容变更，也能监听指定节点是否存在。如果原本节点不存在，那么cache就会在节点创建后触发NodeCacheListener,但是如果该节点被删除，那么Curator就无法触发NodeCacheListener了。
            cache.start(true);

            cache.getListenable().addListener(new NodeCacheListener() {

                public void nodeChanged() throws Exception {
                    byte[] data = cache.getCurrentData().getData();
                    System.out.println(new String(data));
                    System.out.println("这里的数据发生的变化：data="+new String(data));
                }

            });
            //client 不能关闭，要保持连接才能正常监听
            Thread.sleep(Integer.MAX_VALUE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 监听子节点的变化
     * 1.监听的节点必须在监听器添加的时候就要存在，先添加监听器，再添节点可能监听无效
     * 2.只能监听子节点，不能监听当前节点上的数据变化
     */
    @Test
    public   void pathChildrenListner() {

        try {
            CuratorFramework client=CuratorClinetUtil.getConnection();
            PathChildrenCache childrenCache=new PathChildrenCache(client,"/c1",true);
            /**想使用cache，必须调用它的start方法，不用之后调用close方法,
             * start有两个， 其中一个可以传入StartMode，用来为初始的cache设置暖场方式(warm)
             * 1.NORMAL: 初始时为空。
             * 2.BUILD_INITIAL_CACHE: 在这个方法返回之前调用rebuild()。
             * 3.POST_INITIALIZED_EVENT: 当Cache初始化数据后发送一个PathChildrenCacheEvent.Type#INITIALIZED事件，
             * 即当前监控下有多少个子节点就要触发多少次CHILD_ADDED事件
             * */
            childrenCache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);
            PathChildrenCacheListener listener=new PathChildrenCacheListener() {
                @Override
                public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent pathChildrenCacheEvent) throws Exception {
                    System.out.println("Event-name="+pathChildrenCacheEvent.getType().name()+",currentTime="+System.currentTimeMillis());
                    System.out.println("Event-path="+pathChildrenCacheEvent.getData().getPath());
                }
            };
            childrenCache.getListenable().addListener(listener);
            Thread.sleep(Integer.MAX_VALUE);
        } catch (Exception e) {
            e.printStackTrace();
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
