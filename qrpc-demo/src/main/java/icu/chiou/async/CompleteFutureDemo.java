package icu.chiou.async;

/**
 * Author: chiou
 * createTime: 2023/7/26
 * Description: No Description
 */

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CompleteFutureDemo {

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        CompletableFuture<String> completableFutureOne = new CompletableFuture<>();

        ExecutorService cachePool = Executors.newCachedThreadPool();
        cachePool.execute(() -> {
            try {
                Thread.sleep(3000);
                System.out.println("异步任务结果开始存储");
                completableFutureOne.complete("异步任务执行结果");
                Thread.sleep(2000);
                System.out.println("异步任务结果存储到回调了已经过去两秒了");
                System.out.println(Thread.currentThread().getName());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });


        // WhenComplete 方法返回的 CompletableFuture 仍然是原来的 CompletableFuture 计算结果.
        CompletableFuture<String> completableFutureTwo = completableFutureOne.whenComplete((s, throwable) -> {
            System.out.println("当异步任务执行完毕时打印异步任务的执行结果: " + s);
        });
        System.out.println("我没有被阻塞");


        // ThenApply 方法返回的是一个新的 completeFuture.
        CompletableFuture<Integer> completableFutureThree = completableFutureTwo.thenApply(s -> {
            System.out.println("当异步任务执行结束时, 根据上一次的异步任务结果, 继续开始一个新的异步任务!");
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return s.length();
        });

        System.out.println("阻塞方式获取执行结果:" + completableFutureThree.get());
        System.out.println("我被阻塞了");
        cachePool.shutdown();
    }
}

