package org.ming.rpc.Client.proxy;

import lombok.AllArgsConstructor;
import org.ming.rpc.Client.circuitBreaker.CircuitBreaker;
import org.ming.rpc.Client.circuitBreaker.CircuitBreakerProvider;
import org.ming.rpc.Client.retry.GuavaRetry;
import org.ming.rpc.Client.rpcClient.RpcClient;
import org.ming.rpc.Client.rpcClient.impl.NettyRpcClient;
import org.ming.rpc.Client.serviceCenter.ServiceCenter;
import org.ming.rpc.Client.serviceCenter.ZkServiceCenter;
import org.ming.rpc.common.Message.RpcRequest;
import org.ming.rpc.common.Message.RpcResponse;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

@AllArgsConstructor
public class ClientProxy implements InvocationHandler {

    private CircuitBreakerProvider circuitBreakerProvider;
    private RpcClient rpcClient;
    private ServiceCenter serviceCenter;
    public ClientProxy(){
        serviceCenter = new ZkServiceCenter();
        rpcClient=new NettyRpcClient(serviceCenter);
        circuitBreakerProvider=new CircuitBreakerProvider();
    }

    //jdk动态代理，每一次代理对象调用方法，都会经过此方法增强（反射获取request对象，socket发送到服务端）
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //构建request
        RpcRequest request=RpcRequest.builder()
                .interfaceName(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .params(args).paramsType(method.getParameterTypes()).build();
        CircuitBreaker circuitBreaker=circuitBreakerProvider.getCircuitBreaker(method.getName());
        if (!circuitBreaker.allowRequest()){
            return null;  //发生熔断，返回特殊值
        }
        //数据传输
        RpcResponse response;
        //后续添加逻辑：为保持幂等性，只对白名单上的服务进行重试
        if (serviceCenter.checkRetry(request.getInterfaceName())){
            //调用retry框架进行重试操作
            response=new GuavaRetry().sendServiceWithRetry(request,rpcClient);
        }else {
            //只调用一次
            response= rpcClient.sendRequest(request);
        }
        return response.getData();
    }
    public <T>T getProxy(Class<T> clazz){
        Object o = Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, this);
        return (T)o;
    }
}
