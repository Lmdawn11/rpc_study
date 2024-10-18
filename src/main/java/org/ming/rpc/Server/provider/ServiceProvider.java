package org.ming.rpc.Server.provider;

import org.ming.rpc.Server.ratelimit.provider.RateLimitProvider;
import org.ming.rpc.Server.serviceRegister.ServiceRegister;
import org.ming.rpc.Server.serviceRegister.impl.ZkServiceRegister;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * @author wxx
 * @version 1.0
 * @create 2024/2/16 17:35
 */
//本地服务存放器
public class ServiceProvider {
    private Map<String,Object> interfaceProvider;
    //集合中存放服务的实例
    private int port;
    private String host;
    //注册服务类
    private ServiceRegister serviceRegister;
    //限流器
    private RateLimitProvider rateLimitProvider;


    public ServiceProvider(String host,int port){
        //需要传入服务端自身的网络地址
        this.host=host;
        this.port=port;
        this.interfaceProvider=new HashMap<>();
        this.serviceRegister=new ZkServiceRegister();
        this.rateLimitProvider=new RateLimitProvider();
    }
    //本地注册服务

    public void provideServiceInterface(Object service,boolean canRetry){
        String serviceName=service.getClass().getName();
        Class<?>[] interfaceName=service.getClass().getInterfaces();

        for (Class<?> clazz:interfaceName){
            interfaceProvider.put(clazz.getName(),service);
            serviceRegister.register(clazz.getName(),new InetSocketAddress(host,port),canRetry);
        }

    }
    //获取服务实例
    public Object getService(String interfaceName){
        return interfaceProvider.get(interfaceName);
    }
    public RateLimitProvider getRateLimitProvider(){
        return rateLimitProvider;
    }

}
