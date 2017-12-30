跟着实例学习ZooKeeper的用法： 分布式锁
锁
分布式的锁全局同步， 这意味着任何一个时间点不会有两个客户端都拥有相同的锁。

可重入锁Shared Reentrant Lock
首先我们先看一个全局可重入的锁。 Shared意味着锁是全局可见的， 客户端都可以请求锁。 Reentrant和JDK的ReentrantLock类似， 意味着同一个客户端在拥有锁的同时，可以多次获取，不会被阻塞。 它是由类InterProcessMutex来实现。 它的构造函数为：

public InterProcessMutex(CuratorFramework client, String path)

通过acquire获得锁，并提供超时机制：

public void acquire()
Acquire the mutex - blocking until it's available. Note: the same thread can call acquire
re-entrantly. Each call to acquire must be balanced by a call to release()

public boolean acquire(long time,
                       TimeUnit unit)
Acquire the mutex - blocks until it's available or the given time expires. Note: the same thread can
call acquire re-entrantly. Each call to acquire that returns true must be balanced by a call to release()

Parameters:
time - time to wait
unit - time unit
Returns:
true if the mutex was acquired, false if not
通过release()方法释放锁。 InterProcessMutex 实例可以重用。

Revoking ZooKeeper recipes wiki定义了可协商的撤销机制。 为了撤销mutex, 调用下面的方法：

public void makeRevocable(RevocationListener<T> listener)
将锁设为可撤销的. 当别的进程或线程想让你释放锁是Listener会被调用。
Parameters:
listener - the listener
如果你请求撤销当前的锁， 调用Revoker方法。

public static void attemptRevoke(CuratorFramework client,
                                 String path)
                         throws Exception
Utility to mark a lock for revocation. Assuming that the lock has been registered
with a RevocationListener, it will get called and the lock should be released. Note,
however, that revocation is cooperative.
Parameters:
client - the client
path - the path of the lock - usually from something like InterProcessMutex.getParticipantNodes()
错误处理 还是强烈推荐你使用ConnectionStateListener处理连接状态的改变。 当连接LOST时你不再拥有锁。

首先让我们创建一个模拟的共享资源， 这个资源期望只能单线程的访问，否则会有并发问题。