package org.ming.rpc.Client.serviceCenter;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.ming.rpc.Client.cache.serviceCache;
import org.ming.rpc.Client.serviceCenter.ZkWatcher.WatchZk;
import org.ming.rpc.Client.serviceCenter.balance.impl.ConsistencyHashBalance;

import java.net.InetSocketAddress;
import java.util.List;

public class ZkServiceCenter implements ServiceCenter{
    //curator提供的zk客户端
    private CuratorFramework client;
    private static final String ROOT_PATH = "MyRPC";
    private serviceCache cache;
    private static final String RETRY = "CanRetry";

    public ZkServiceCenter() {
        RetryPolicy policy = new ExponentialBackoffRetry(1000, 3);
        this.client = CuratorFrameworkFactory.builder()
                .connectString("120.46.49.81:2181")
                .sessionTimeoutMs(40000)
                .retryPolicy(policy)
                .namespace(ROOT_PATH).build();
        this.client.start();
        System.out.println("zk连接成功");
        cache=new serviceCache();
        WatchZk watchZk = new WatchZk(client, cache);
        watchZk.watchToUpdate(ROOT_PATH);
    }

    @Override
    public InetSocketAddress serviceDiscovery(String serviceName) {
        try {
            List<String> servcieList = cache.getServcieFromCache(serviceName);
            if(servcieList==null){
                servcieList = client.getChildren().forPath("/" + serviceName);
            }
            String s = new ConsistencyHashBalance().balance(servcieList);
//            String s = servcieList.get(0);
            return parseAddress(s);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean checkRetry(String serviceName) {
        boolean canRetry = false;
        try {
            List<String> serviceList = client.getChildren().forPath("/" + RETRY);
            for(String s:serviceList){
                if(s.equals(serviceName)){
                    System.out.println("服务"+serviceName+"在白名单上，可进行重试");
                    canRetry=true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return canRetry;
    }

    // 地址 -> XXX.XXX.XXX.XXX:port 字符串
    private String getServiceAddress(InetSocketAddress serverAddress) {
        return serverAddress.getHostName() +
                ":" +
                serverAddress.getPort();
    }
    // 字符串解析为地址
    private InetSocketAddress parseAddress(String address) {
        String[] result = address.split(":");
        return new InetSocketAddress(result[0], Integer.parseInt(result[1]));
    }
}
