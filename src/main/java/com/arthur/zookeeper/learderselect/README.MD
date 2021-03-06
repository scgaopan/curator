
相信你在使用ZK API开发时会遇到让人头疼的几个问题，ZK连接管理、SESSION失效等一些异常问题的处理，Curator替我们解决了这些问题，
通过对ZK连接状态的监控来做出相应的重连等操作，并触发事件。 更好的地方是Curator对ZK的一些应用场景提供了非常好的实现，而且有很多扩充，
这些都符合ZK使用规范。 它的主要组件为：

Recipes， ZooKeeper的系列recipe实现, 基于 Curator Framework.
Framework， 封装了大量ZooKeeper常用API操作，降低了使用难度, 基于Zookeeper增加了一些新特性，对ZooKeeper链接的管理，对链接丢失自动重新链接。
Utilities，一些ZooKeeper操作的工具类包括ZK的集群测试工具路径生成等非常有用，在Curator-Client包下org.apache.curator.utils。
Client，ZooKeeper的客户端API封装，替代官方 ZooKeeper class，解决了一些繁琐低级的处理，提供一些工具类。
Errors，异常处理, 连接异常等
Extensions，对curator-recipes的扩展实现，拆分为 curator-:stuck_out_tongue_closed_eyes:iscovery和 curator-:stuck_out_tongue_closed_eyes:iscovery-server提供基于RESTful的Recipes WEB服务.


leader选举（有两种方式）
在分布式计算中， leader election是很重要的一个功能， 这个选举过程是这样子的： 指派一个进程作为组织者，将任务分发给各节点。 在任务开始前， 哪个节点都不知道谁是leader或者coordinator. 当选举算法开始执行后， 每个节点最终会得到一个唯一的节点作为任务leader. 除此之外， 选举还经常会发生在leader意外宕机的情况下，新的leader要被选举出来。

Curator 有两种选举recipe， 你可以根据你的需求选择合适的。

1.Leader latch
首先我们看一个使用LeaderLatch类来选举的例子。 它的构造函数如下：

public LeaderLatch(CuratorFramework client, String latchPath)
public LeaderLatch(CuratorFramework client, String latchPath,  String id)
必须启动LeaderLatch: leaderLatch.start(); 一旦启动， LeaderLatch会和其它使用相同latch path的其它LeaderLatch交涉，然后随机的选择其中一个作为leader。 你可以随时查看一个给定的实例是否是leader:

public boolean hasLeadership()
类似JDK的CountDownLatch， LeaderLatch在请求成为leadership时有block方法：

public void await()
          throws InterruptedException,
                 EOFException
Causes the current thread to wait until this instance acquires leadership
unless the thread is interrupted or closed.

public boolean await(long timeout,
                     TimeUnit unit)
             throws InterruptedException
一旦不使用LeaderLatch了，必须调用close方法。 如果它是leader,会释放leadership， 其它的参与者将会选举一个leader。

异常处理 LeaderLatch实例可以增加ConnectionStateListener来监听网络连接问题。 当 SUSPENDED 或 LOST 时, leader不再认为自己还是leader.
当LOST 连接重连后 RECONNECTED,LeaderLatch会删除先前的ZNode然后重新创建一个. LeaderLatch用户必须考虑导致leadershi丢失的连接问题。 强烈推荐你使用ConnectionStateListener。




2.Leader Election
  Curator还提供了另外一种选举方法。 注意涉及以下四个类：
  
  LeaderSelector
  LeaderSelectorListener
  LeaderSelectorListenerAdapter
  CancelLeadershipException
  重要的是LeaderSelector类，它的构造函数为：
  
  public LeaderSelector(CuratorFramework client, String mutexPath,LeaderSelectorListener listener)
  public LeaderSelector(CuratorFramework client, String mutexPath, ThreadFactory threadFactory, Executor executor, LeaderSelectorListener listener)
  类似LeaderLatch,必须start: leaderSelector.start(); 一旦启动，当实例取得领导权时你的listener的takeLeadership()方法被调用. 而takeLeadership()方法只有领导权被释放时才返回。 当你不再使用LeaderSelector实例时，应该调用它的close方法。
  
  异常处理 LeaderSelectorListener类继承ConnectionStateListener.LeaderSelector必须小心连接状态的 改变. 如果实例成为leader, 它应该相应SUSPENDED 或 LOST. 当 SUSPENDED 状态出现时， 实例必须假定在重新连接成功之前它可能不再是leader了。 如果LOST状态出现， 实例不再是leader， takeLeadership方法返回.
  
  重要: 推荐处理方式是当收到SUSPENDED 或 LOST时抛出CancelLeadershipException异常. 这会导致LeaderSelector实例中断并取消执行takeLeadership方法的异常. 这非常重要， 你必须考虑扩展LeaderSelectorListenerAdapter. LeaderSelectorListenerAdapter提供了推荐的处理逻辑。