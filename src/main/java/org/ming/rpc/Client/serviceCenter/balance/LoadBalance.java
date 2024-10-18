package org.ming.rpc.Client.serviceCenter.balance;

import java.util.List;

public interface LoadBalance {
    String balance(List<String> addressList);
    void addNode(String node) ;
    void delNode(String node);
}
