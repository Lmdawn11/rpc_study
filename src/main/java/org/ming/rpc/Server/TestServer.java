package org.ming.rpc.Server;


import org.ming.rpc.Server.provider.ServiceProvider;
import org.ming.rpc.Server.server.RpcServer;
import org.ming.rpc.common.service.Impl.UserServiceImpl;
import org.ming.rpc.common.service.UserService;
import org.ming.rpc.Server.server.impl.NettyRPCRPCServer;

/**
 * @author wxx
 * @version 1.0
 * @create 2024/2/11 19:39
 */
public class TestServer {
    public static void main(String[] args) {
        UserService userService=new UserServiceImpl();

        ServiceProvider serviceProvider=new ServiceProvider("127.0.0.1",9999);
        serviceProvider.provideServiceInterface(userService,true);

        RpcServer rpcServer=new NettyRPCRPCServer(serviceProvider);
        rpcServer.start(9999);
    }
}
