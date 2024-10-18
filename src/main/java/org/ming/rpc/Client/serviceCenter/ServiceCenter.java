package org.ming.rpc.Client.serviceCenter;

import java.net.InetSocketAddress;

public interface ServiceCenter {
    InetSocketAddress serviceDiscovery(String serviceName);

    boolean checkRetry(String serviceName) ;
}