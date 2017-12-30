ZooKeeper的用法： Barrier

分布式Barrier是这样一个类： 它会阻塞所有节点上的等待进程，直到某一个条件被满足， 然后所有的节点继续进行。

比如赛马比赛中， 等赛马陆续来到起跑线前。 一声令下，所有的赛马都飞奔而出

栅栏Barrier
DistributedBarrier类实现了栅栏的功能。 它的构造函数如下：

public DistributedBarrier(CuratorFramework client, String barrierPath)
Parameters:
client - client
barrierPath - path to use as the barrier
首先你需要设置栅栏，它将阻塞在它上面等待的线程:

setBarrier();
然后需要阻塞的线程调用“方法等待放行条件:

public void waitOnBarrier()
当条件满足时，移除栅栏，所有等待的线程将继续执行：

removeBarrier();
异常处理 DistributedBarrier 会监控连接状态，当连接断掉时waitOnBarrier()方法会抛出异常。。




双栅栏Double Barrier
双栅栏允许客户端在计算的开始和结束时同步。当足够的进程加入到双栅栏时，进程开始计算， 当计算完成时，离开栅栏。 双栅栏类是DistributedDoubleBarrier。 构造函数为:

public DistributedDoubleBarrier(CuratorFramework client,
                                String barrierPath,
                                int memberQty)
Creates the barrier abstraction. memberQty is the number of members in the barrier. When enter() is called, it blocks until
all members have entered. When leave() is called, it blocks until all members have left.

Parameters:
client - the client
barrierPath - path to use
memberQty - the number of members in the barrier
memberQty是成员数量，当enter方法被调用时，成员被阻塞，直到所有的成员都调用了enter。 当leave方法被调用时，它也阻塞调用线程， 直到所有的成员都调用了leave。 就像百米赛跑比赛， 发令枪响， 所有的运动员开始跑，等所有的运动员跑过终点线，比赛才结束。

DistributedBarrier 会监控连接状态，当连接断掉时enter()和leave方法会抛出异常。