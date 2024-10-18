package org.ming.rpc.Server.ratelimit.impl;

import org.ming.rpc.Server.ratelimit.RateLimit;

public class TokenBucketRateLimitImpl implements RateLimit {
    //令牌产生速率（单位为ms）
    private static  int RATE;
    //桶容量
    private static  int CAPACITY;
    //当前桶容量
    private volatile int curCapcity;
    //时间戳
    private volatile long timeStamp=System.currentTimeMillis();
    public TokenBucketRateLimitImpl(int rate,int capacity){
        RATE=rate;
        CAPACITY=capacity;
        curCapcity=capacity;
    }
    @Override
    public boolean getToken() {
        if (curCapcity>0){
            curCapcity--;
            return true;
        }
        long current = System.currentTimeMillis();
        if (current-timeStamp>=RATE){
            if ((current-timeStamp)/RATE>=2){
                curCapcity+=(int)(current-timeStamp)/RATE-1;
            }
            if(curCapcity>CAPACITY) curCapcity=CAPACITY;
            //刷新时间戳为本次请求
            timeStamp=current;
            return true;
        }
        return false;
    }
}
