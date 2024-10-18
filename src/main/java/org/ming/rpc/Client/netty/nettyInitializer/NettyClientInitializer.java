package org.ming.rpc.Client.netty.nettyInitializer;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import org.ming.rpc.Client.netty.handler.NettyClientHandler;
import org.ming.rpc.common.mySerializer.JsonSerializer;
import org.ming.rpc.common.mycode.MyDecoder;
import org.ming.rpc.common.mycode.MyEncoder;

public class NettyClientInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        ch.pipeline().addLast(new MyDecoder());
        ch.pipeline().addLast(new MyEncoder(new JsonSerializer()));
        pipeline.addLast(new NettyClientHandler());
    }
}
