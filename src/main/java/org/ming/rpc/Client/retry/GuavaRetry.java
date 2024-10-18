package org.ming.rpc.Client.retry;

import com.github.rholder.retry.*;
import org.ming.rpc.Client.rpcClient.RpcClient;
import org.ming.rpc.common.Message.RpcRequest;
import org.ming.rpc.common.Message.RpcResponse;

import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class GuavaRetry {
    private RpcClient rpcClient;
    public RpcResponse sendServiceWithRetry(RpcRequest request, RpcClient rpcClient) {
        this.rpcClient = rpcClient;
        Retryer<RpcResponse> retryer = RetryerBuilder.<RpcResponse>newBuilder()
                //任何异常都重试
                .retryIfException()
                //结果是500也重试
                .retryIfResult(response -> Objects.equals(response.getCode(), 500))
                //重试策略 等待2s在重试
                .withWaitStrategy(WaitStrategies.fixedWait(2, TimeUnit.SECONDS))
                // 重试次数
                .withStopStrategy(StopStrategies.stopAfterAttempt(3))
                .withRetryListener(new RetryListener() {
                    @Override
                    public <V> void onRetry(Attempt<V> attempt) {
                        System.out.println("RetryListener: 第" + attempt.getAttemptNumber() + "次调用");
                    }
                })
                .build();
        try {
            return retryer.call(()->{
                return rpcClient.sendRequest(request);
            });
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (RetryException e) {
            e.printStackTrace();
        }
        return RpcResponse.fail();
    }
}
