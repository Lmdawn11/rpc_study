package org.ming.rpc.Client.serviceCenter.balance.impl;

import org.ming.rpc.Client.serviceCenter.balance.LoadBalance;

import java.util.List;

public class RoundLoadBalance implements LoadBalance {
    private int choose = -1;
    @Override
    public String balance(List<String> addressList) {
        choose++;
        choose%=addressList.size();
        System.out.println("负载均衡选择了 {} 服务器"+choose);
        return addressList.get(choose);
    }

    @Override
    public void addNode(String node) {

    }

    @Override
    public void delNode(String node) {

    }
}
