package icu.chiou.async;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Author: chiou
 * createTime: 2023/7/26
 * Description: 获取异步任务结果类
 */
public class MyCompleteFuture {
    public static void main(String[] args) throws ExecutionException, InterruptedException, TimeoutException {
        CompletableFuture<Integer> completableFuture = new CompletableFuture<>();
        new Thread(() -> {
            int i = 0;
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            i = 8;
            completableFuture.complete(i);
        }).start();
        Integer integer = completableFuture.get(3, TimeUnit.SECONDS);
        System.out.println("integer = " + integer);
        System.out.println("main-我被阻塞了");
    }
}
