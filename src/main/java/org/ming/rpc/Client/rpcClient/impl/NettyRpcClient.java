package org.ming.rpc.Client.rpcClient.impl;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.ming.rpc.Client.netty.nettyInitializer.NettyClientInitializer;
import org.ming.rpc.Client.rpcClient.RpcClient;
import org.ming.rpc.Client.serviceCenter.ServiceCenter;
import org.ming.rpc.common.Message.RpcRequest;
import org.ming.rpc.common.Message.RpcResponse;

import java.net.InetSocketAddress;

@Slf4j
public class NettyRpcClient implements RpcClient {

    private static final Bootstrap bootstrap ;
    private static final EventLoopGroup eventLoopGroup ;
    private ServiceCenter serviceCenter;
    public NettyRpcClient(ServiceCenter serviceCenter){
        this.serviceCenter=serviceCenter;
    }
    static {
        bootstrap = new Bootstrap();
        eventLoopGroup = new NioEventLoopGroup();
        bootstrap
                .group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new NettyClientInitializer());
    }

    @Override
    public RpcResponse sendRequest(RpcRequest request) {
        InetSocketAddress address = serviceCenter.serviceDiscovery(request.getInterfaceName());
        String host = address.getHostName();
        int port = address.getPort();

        try {
            //创建一个channelFuture对象，代表这一个操作事件，sync方法表示堵塞直到connect完成
            ChannelFuture channelFuture = bootstrap.connect(host, port).sync();
            System.out.println("客户端连接成功");
            //channel表示一个连接单位，类似与socket
            Channel channel = channelFuture.channel();
            //发送消息
            channel.writeAndFlush(request);
            System.out.println("发送消息成功");
            //sync()堵塞获取结果
            channel.closeFuture().sync();
            // 阻塞的获得结果，通过给channel设计别名，获取特定名字下的channel中的内容（这个在hanlder中设置）
            // AttributeKey是，线程隔离的，不会由线程安全问题。
            // 当前场景下选择堵塞获取结果
            // 其它场景也可以选择添加监听器的方式来异步获取结果 channelFuture.addListener...
            AttributeKey<RpcResponse> key = AttributeKey.valueOf("RPCResponse");
            RpcResponse rpcResponse = channel.attr(key).get();
            return rpcResponse;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }
}
