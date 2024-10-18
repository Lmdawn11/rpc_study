# 项目介绍

本项目参考https://github.com/youngyangyang04/RPC-Java的设计与实现，后续会继续完成多种序列化方式及其他优化

# 项目总结

# NIO

## 启动剖析

```java
//1 netty 中使用 NioEventLoopGroup （简称 nio boss 线程）来封装线程和 selector
Selector selector = Selector.open(); 

//2 创建 NioServerSocketChannel，同时会初始化它关联的 handler，以及为原生 ssc 存储 config
NioServerSocketChannel attachment = new NioServerSocketChannel();

//3 创建 NioServerSocketChannel 时，创建了 java 原生的 ServerSocketChannel
ServerSocketChannel serverSocketChannel = ServerSocketChannel.open(); 
serverSocketChannel.configureBlocking(false);

//4 启动 nio boss 线程执行接下来的操作

//5 注册（仅关联 selector 和 NioServerSocketChannel），未关注事件
SelectionKey selectionKey = serverSocketChannel.register(selector, 0, attachment);

//6 head -> 初始化器 -> ServerBootstrapAcceptor -> tail，初始化器是一次性的，只为添加 acceptor

//7 绑定端口
serverSocketChannel.bind(new InetSocketAddress(8080));

//8 触发 channel active 事件，在 head 中关注 op_accept 事件
selectionKey.interestOps(SelectionKey.OP_ACCEPT);
```

### Selector selector = Selector.open(); 

1.1 init main

```java
final ChannelFuture regFuture = initAndRegister();
```

创建NioServerSocketChannel main

```java
NioServerSocketChannel attachment = new NioServerSocketChannel();
```

添加NioServerSocketChannel 初始化handler  main

初始化handler等待调用  

1.2 register

启动Nio boss线程 main main

**通过判断当前线程是否是主线程，切换至nio线程**

原生ssc注册至selector未关注事件 nio thread

**selectionKey = javaChannel().register(eventLoop().unwrappedSelector(), 0, this);**

执行NioServerSocketChannel初始化 handler  nio thread



1.3 regFuture等待回调执行dobind0 nio-thread

绑定了原生的ssc，和一些配置

```java
serverSocketChannel.bind(new InetSocketAddress(8080));
```

原生NioServerSocketChanne  active事件 nio thread

# netty的启动流程

 ![image-20241017160237808](C:\Users\liming\AppData\Roaming\Typora\typora-user-images\image-20241017160237808.png)

# eventloop的selector何时创建  

****

**一个eventloop只包含一个线程，nioeventloop的重要组成包括selector、线程、任务队列，既会处理io事件，优惠处理普通和定时任务**

- select何时创建

  ​       

  ```java
   final SelectorTuple selectorTuple = openSelector();  此时创建执行下列方法
       
           private SelectorTuple openSelector() {
          final Selector unwrappedSelector;
          try {
              unwrappedSelector = provider.openSelector();
  ```

  

  - selector为何有两个selector成员

    ```java
        private Selector selector;			\\优化之后的
        private Selector unwrappedSelector;  \\系统创建的
    ```

    原因是  unwrappedSelector中以set的方式存入关注的selectkeys，基于hash表的实现遍历效率不高

    selector 替换了这种方法，基于数组的方式存入selectkeys  使得遍历selectkeys提高效率

- nio线程何时启动

  **通过判断状态位，首次调用execute方法时启动**，启动之后修改状态位 

- nio中的selector.select()和netty中的Selector.select(timeoutmills)不一样

  **netty中不仅要处理io任务，普通任务也要处理，超时之后，结束阻塞，检测是否有普通任务**

  **nio中则不需要，所有一直阻塞直到连接建立触发accept事件**

- 提交普通任务会不会结束select阻塞

  **会，会调用selector.wakeup()唤醒**

  ```java
  @Override
  protected void wakeup(boolean inEventLoop) {
      if (!inEventLoop && wakenUp.compareAndSet(false, true)) {
          selector.wakeup();
      }
  }
  ```

  - **wakeup方法中的代码如何理解**

    只有其它线程提交任务时，才会唤醒selector

  - **wakenUp变量作用是什么**

    只要唤醒一次就行，如果有多个线程都提交了任务，selector唤醒了，处理任务队列中的任务

    为了避免wakeup被频繁调用，使用cas锁，只有一个线程能成功

- 每次循环时，什么时候进入selectStrategy.Select分支

  没有任务时，才会进入

  当有任务时，会调用select.Now方法，拿到selector所有key

  - 何时会阻塞select，阻塞多久

    没有定时任务时，会被阻塞 

    当超时时间到了、有任务了会结束阻塞

- nio空轮询体现在那，如何解决

  selector不会被阻塞，一直占用cpu，解决:重新创建一个selector，替换旧的selector

- ioRate控制什么，设置100有何作用

  io事件跟普通事件时间的处理比例，100会导致所有普通任务运行完

- selectedKey优化怎么回事

- 在哪里区分不同事件类型

  在一个processselectkeys区分各种select事件

# RPC项目架构

 ![image-20241017173107727](C:\Users\liming\AppData\Roaming\Typora\typora-user-images\image-20241017173107727.png)

## 1.基础模型

netty的工作原理:Nioeventloop，初始化流程

协议格式:

序列化方法：

## 2.zookeeper

本地服务缓存:设置、更新



## 3 负载均衡

负载均衡是由于服务器服务调用太频繁，为了维护服务器稳定而提出来的

有：轮询法，随机法，一致性hash算法

轮询法的缺点是，太平均了，故而难以充分发挥服务器性能

随机法：随机性太强，不稳定

所以实现的是一致性hash算法：

就是设置一个hash环，和虚拟节点，每个具体的服务器-绑定多个虚拟节点，这些虚拟节点映射到hash环上，请求服务时，遍历到最近的hash节点，找到对应的具体服务器，实现远程调用

后续优化：自适应负载均衡

## 4限流器

限流器是当服务器负载压力过大时，触发的一种保护机制，那就不让它再接收太多的请求就好了，等接收和处理的请求数量下来后，这个节点的负载压力自然就下来了。

常见的限流算法有:计数器法、滑动窗口法、露桶算法、令牌桶算法

计数器法的缺点:

面对突发大量请求，有限制，用户体验不好

限流不够平滑，假设1min时长的滑动窗口，前30s处理30个请求，后面再来了30个，无法处理

滑动窗口缺点：

滑动窗口计数器算法依然存在限流不够平滑的问题

漏桶算法:

无法应对突然激增的流量，因为只能以固定的速率处理请求，对系统资源利用不够友好。

桶流入水（发请求）的速率如果一直大于桶流出水（处理请求）的速率的话，那么桶会一直是满的，一部分新的请求会被丢弃，导致服务质量下降。

令牌桶算法:以一定速率产生令牌，装入桶中，一个接受到的请求对应一个令牌



## 5熔断器-保护服务

客户端多级调用服务器中的服务，比如A->B-C  此时C宕机了，A一直调用B，B无法从C获得结果返回给A，导致B堆积了大量未处理请求而宕机。服务 B 调用服务 C，服务 C 执行业务逻辑出现异常时，会影响到服务 B，甚至可能会引起服务 B 宕机。这还只是 A->B->C 的情况，试想一下 A->B->C->D->……呢？在整个调用链中，只要中间有一个服务出现问题，都可能会引起上游的所有服务出现一系列的问题，甚至会引起整个调用链的服务都宕机，这是非常恐怖的。

所以说，在一个服务作为调用端调用另外一个服务时，为了防止被调用的服务出现问题而影响到作为调用端的这个服务，这个服务也需要进行自我保护。而最有效的自我保护方式就是熔断。

所以设计一个熔断器，包含3种状态

**闭合**：正常情况下，后台对失败次数进行积累，到达一定阈值时自动熔断

**断开**:一旦对服务的调用失败次数达到一定阈值时，熔断器就会打开，这时候对服务的调用将直接返回一个预定的错误，而不执行真正的网络调用。同时，熔断器需要设置一个固定的时间间隔，当处理请求达到这个时间间隔时会进入半熔断状态。

**半开**：在半开状态下，熔断器会对通过它的部分请求进行处理，如果对这些请求的成功处理数量达到一定比例则认为服务已恢复正常，就会关闭熔断器，反之就会打开熔断器。

## 6超时重传 &白名单

超时重传是因为客户端向服务器传输信息时，由于网络问题，导致发送失败，此时触发超时重传

什么样的业务会触发超时重传呢，会不会导致问题？比如插入，查询等

所以设置了白名单，服务端在注册节点时，将业务的幂等性也注册进去，客户端请求服务，先去白名单查看是否幂等服务，如果是则用重试框架调用传输，如果不是则正常传输

如何设置超时重传呢？

使用了google的Guava框架设计了重传策略

任何异常，返回值发生重传，

重试策略等待2s重传，重试3次

