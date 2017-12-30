package com.arthur.zookeeper.learderselect;

import com.sun.tools.doclets.formats.html.SourceToHTMLConverter;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Leader Election方式
 *
 * Created by gaopan on 17/12/24.
 */
public class ExampleClient extends LeaderSelectorListenerAdapter implements Closeable {

    private  final String name;

    private final LeaderSelector leaderSelector;
    private final AtomicInteger leaderCount=new AtomicInteger();

    public ExampleClient(CuratorFramework client,String path,String name) {
        this.name=name;
        leaderSelector=new LeaderSelector(client,path,this);
        leaderSelector.autoRequeue();//自动去抢leadership
        //leaderSelector.autoRequeue();保证在此实例释放领导权之后还可能获得领导权。 在这里我们使用AtomicInteger来记录此client获得领导权的次数， 它是”fair”， 每个client有平等的机会获得领导权。
    }

    public void start() throws IOException{
        leaderSelector.start();//使用leaderSerlect时必须先启动它
    }
    @Override
    public void close() throws IOException {

        leaderSelector.close();
    }

    @Override
    public void takeLeadership(CuratorFramework client) throws Exception {

        final  int waitSeconds=(int) (5*Math.random())+1;
        System.out.println(name + " 目前是leader. 等待 " + waitSeconds + " seconds...后会释放leadership");
        System.out.println(name + " 已经是第 " + leaderCount.getAndIncrement() + " time(s)成为leader(index从0开始).");
        try {
            //你可以在此进行任务的分配等等，并且不要返回，如果你想要要此实例一直是leader的话可以加一个死循环。
            Thread.sleep(TimeUnit.SECONDS.toMillis(waitSeconds));
        } catch (InterruptedException e) {
            System.err.println(name + " was interrupted.");
            Thread.currentThread().interrupt();
        } finally {
            System.out.println(name + " 放弃 leadership.\n");
        }
    }
}
