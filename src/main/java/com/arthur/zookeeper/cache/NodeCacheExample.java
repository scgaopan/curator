package com.arthur.zookeeper.cache;

import com.arthur.zookeeper.CuratorClinetUtil;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.utils.CloseableUtils;
import org.apache.zookeeper.KeeperException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;

/**
 * Created by gaopan on 17/12/24.
 */
public class NodeCacheExample {

    private  static final  String PATH="/example/nodeCache";

    public static void main(String[] args) throws  Exception{
        CuratorFramework curatorFramework= CuratorClinetUtil.getConnection();
        NodeCache cache=null;
        try {
            //curatorFramework.start();
            cache=new NodeCache(curatorFramework,PATH);
            cache.start();
            processCommands(curatorFramework,cache);
        } finally {
            CloseableUtils.closeQuietly(cache);
            CloseableUtils.closeQuietly(curatorFramework);

        }

    }




    private  static  void processCommands(CuratorFramework client,NodeCache cache) throws  Exception{
        printHelp();

        try {
            addListener(cache);//结当前节点添加监听器
            BufferedReader in=new BufferedReader(new InputStreamReader(System.in));
            boolean done=false;
            while (!done){
                System.out.println(">");
                String line=in.readLine();
                if(line==null){
                    break;
                }
                String command=line.trim();
                String[] parts=command.split("\\s");//空格作分隔符
                if(parts.length==0){
                    continue;
                }
                String operation=parts[0];
                String args[]= Arrays.copyOfRange(parts,1,parts.length);
                if (operation.equalsIgnoreCase("help") || operation.equalsIgnoreCase("?")) {
                    printHelp();
                } else if (operation.equalsIgnoreCase("q") || operation.equalsIgnoreCase("quit")) {
                    done = true;
                } else if (operation.equals("set")) {
                    setValue(client, command, args);
                } else if (operation.equals("remove")) {
                    remove(client);
                } else if (operation.equals("show")) {
                    show(cache);
                }
                Thread.sleep(1000); // just to allow the console output to catch
                // up
            }
        } catch (Exception e) {

            e.printStackTrace();
        }
    }


    /**
     * 读取数据
     * @param cache
     */
    private static void show(NodeCache cache) {
        if (cache.getCurrentData() != null)
            System.out.println(cache.getCurrentData().getPath() + " = " + new String(cache.getCurrentData().getData()));
        else
            System.out.println("cache don't set a value");
    }




    /**
     * 结结节添加监听器
     * @param cache
     */
    private  static  void addListener(final NodeCache cache){

        NodeCacheListener listener=new NodeCacheListener() {
            public void nodeChanged() throws Exception {
            if(cache.getCurrentData()!=null){
                System.out.println("Node Changed:path="+cache.getCurrentData().getPath()+
                ",current value="+new String(cache.getCurrentData().getData()));
            }

            }
        };
        cache.getListenable().addListener(listener);
    }


    /**
     * 删除数据
     * @param client
     * @throws Exception
     */
    private static void remove(CuratorFramework client) throws Exception {
        try {
            client.delete().forPath(PATH);
        } catch (KeeperException.NoNodeException e) {
            // ignore
        }
    }


    /**
     * 修改数据
     * @param client
     * @param command
     * @param args
     * @throws Exception
     */
    private static void setValue(CuratorFramework client, String command, String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("syntax error (expected set <value>): " + command);
            return;
        }

        byte[] bytes = args[0].getBytes();
        try {
            client.setData().forPath(PATH, bytes);
        } catch (KeeperException.NoNodeException e) {
            client.create().creatingParentsIfNeeded().forPath(PATH, bytes);
        }
    }

    /**
     * 打印帮助
     */
    private  static  void printHelp(){
        System.out.println("An example of using PathChildrenCache. This example is driven by entering commands at the prompt:\n");
        System.out.println("set <value>: Adds or updates a node with the given name");
        System.out.println("remove: Deletes the node with the given name");
        System.out.println("show: Display the node's value in the cache");
        System.out.println("quit: Quit the example");
        System.out.println();
    }
}
