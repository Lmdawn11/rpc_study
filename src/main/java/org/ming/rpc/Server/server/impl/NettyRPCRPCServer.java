package org.ming.rpc.Server.server.impl;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.AllArgsConstructor;
import org.ming.rpc.Server.netty.nettyInitializer.NettyServerInitializer;
import org.ming.rpc.Server.server.RpcServer;
import org.ming.rpc.Server.provider.ServiceProvider;

@AllArgsConstructor
public class NettyRPCRPCServer implements RpcServer {

    private ServiceProvider serviceProvider;
    @Override
    public void start(int port) {
        NioEventLoopGroup bootgroup = new NioEventLoopGroup();
        NioEventLoopGroup workergroup = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap()
                    .group(bootgroup, workergroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new NettyServerInitializer(serviceProvider));
                    System.out.println("服务端netty启动了");
            //阻塞直到连接建立
            ChannelFuture future = serverBootstrap.bind(port).sync();
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            bootgroup.shutdownGracefully();
            workergroup.shutdownGracefully();
        }

    }

    @Override
    public void stop() {

    }
}
