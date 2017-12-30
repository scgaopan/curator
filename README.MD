 一、可以利用ZooKeeper在集群的各个节点之间缓存数据。 每个节点都可以得到最新的缓存的数据。
   Curator提供了三种类型的缓存方式：Path Cache,Node Cache 和Tree Cache。
   
   1.Path Cache
   Path Cache用来监控一个ZNode的子节点. 当一个子节点增加， 更新，删除时， Path Cache会改变它的状态， 会包含最新的子节点，
   子节点的数据和状态。 这也正如它的名字表示的那样， 那监控path。
   监听的节点(当前节点)必须在监听器添加的时候就要存在，先添加监听器，再添节点可能监听无效
   只能监听子节点，不能监听当前节点上的数据变化
   
   实际使用时会涉及到四个类：
   
   PathChildrenCache
   PathChildrenCacheEvent
   PathChildrenCacheListener
   ChildData
   
   通过下面的构造函数创建Path Cache:
   
   public PathChildrenCache(CuratorFramework client, String path, boolean cacheData)
   想使用cache，必须调用它的start方法，不用之后调用close方法。 start有两个， 其中一个可以传入StartMode，用来为初始的cache设置暖场方式(warm)：
   
   NORMAL: 初始时为空。
   BUILD_INITIAL_CACHE: 在这个方法返回之前调用rebuild()。
   POST_INITIALIZED_EVENT: 当Cache初始化数据后发送一个PathChildrenCacheEvent.Type#INITIALIZED事件，即当前监控下有多少个子节点就要触发多少次CHILD_ADDED事件
   PathChildrenCacheEvent.Type：
   CHILD_ADDED 子节点创建
   CHILD_UPDATED 子节点数据变更
   CHILD_REMOVED 子节点删除
   
   public void addListener(PathChildrenCacheListener listener)可以增加listener监听缓存的改变。
   
   getCurrentData()方法返回一个List<ChildData>对象，可以遍历所有的子节点。
   
   
   2.Node Cache
     Path Cache用来监控一个ZNode. 当节点的数据修改或者删除时，Node Cache能更新它的状态包含最新的改变。
     
     涉及到下面的三个类：
     
     NodeCache
     NodeCacheListener
     ChildData
     想使用cache，依然要调用它的start方法，不用之后调用close方法。
     
     getCurrentData()将得到节点当前的状态，通过它的状态可以得到当前的值。 
     可以使用public void addListener(NodeCacheListener listener)监控节点状态的改变。
     
   3.Tree Node
     这种类型的即可以监控节点的状态，还监控节点的子节点的状态， 类似上面两种cache的组合。 这也就是Tree的概念。 它监控整个树中节点的状态。 涉及到下面四个类。
     
     TreeCache
     TreeCacheListener
     TreeCacheEvent
     ChildData
     而关键的TreeCache的构造函数为
     
     public TreeCache(CuratorFramework client, String path, boolean cacheData)
     想使用cache，依然要调用它的start方法，不用之后调用close方法。
     
     getCurrentChildren()返回cache的状态，类型为Map<String, ChildData>。 而getCurrentData()返回监控的path的数据。
     
     public void addListener(TreeCacheListener listener)可以增加listener来监控状态的改变。

