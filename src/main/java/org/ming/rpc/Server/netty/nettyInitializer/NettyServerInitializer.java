package org.ming.rpc.Server.netty.nettyInitializer;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import lombok.AllArgsConstructor;
import org.ming.rpc.Server.netty.handler.NettyServiceHandler;
import org.ming.rpc.Server.provider.ServiceProvider;
import org.ming.rpc.common.mySerializer.JsonSerializer;
import org.ming.rpc.common.mycode.MyDecoder;
import org.ming.rpc.common.mycode.MyEncoder;

@AllArgsConstructor
public class NettyServerInitializer extends ChannelInitializer<SocketChannel> {
    private ServiceProvider serviceProvider;
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new MyEncoder(new JsonSerializer()));
        pipeline.addLast(new MyDecoder());
        //自定义处理器
        pipeline.addLast(new NettyServiceHandler(serviceProvider));

    }
}
