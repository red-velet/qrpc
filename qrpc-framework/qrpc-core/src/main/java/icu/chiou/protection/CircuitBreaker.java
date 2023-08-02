package icu.chiou.protection;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Author: chiou
 * createTime: 2023/8/1
 * Description: 熔断器
 */
public class CircuitBreaker {
    // 状态：用volatile保证多线程可见性
    private volatile boolean isOpen = false;

    // 总的请求数
    private AtomicInteger requestCount = new AtomicInteger();

    // 异常的请求数
    private AtomicInteger errorRequestCount = new AtomicInteger();

    // 容忍的异常比例-异常阈值
    private int maxErrorRequestCount;
    private float maxErrorRequestRate;

    /**
     * 构造函数
     *
     * @param maxErrorRequestCount 最大容忍的异常请求数
     * @param maxErrorRequestRate  最大容忍的异常比例（0-100）%
     */
    public CircuitBreaker(int maxErrorRequestCount, float maxErrorRequestRate) {
        this.maxErrorRequestCount = maxErrorRequestCount;
        this.maxErrorRequestRate = maxErrorRequestRate;
    }

    /**
     * 记录请求总数
     */
    public void recordRequestCount() {
        this.requestCount.getAndIncrement();
    }

    /**
     * 记录异常请求总数
     */
    public void recordErrorRequestCount() {
        this.errorRequestCount.getAndIncrement();
    }

    /**
     * 查看断路器状态
     *
     * @return 断路器状态 true-开启（熔断状态） false-关闭
     */
    public Boolean isBreak() {
        // 已经打开
        if (isOpen) {
            return true;
        }

        // 查看当前指标是否达到异常阈值
        if (errorRequestCount.get() > maxErrorRequestCount) {
            // 超过最大异常请求数，打开断路器
            this.isOpen = true;
            return true;
        }

        if (errorRequestCount.get() > 0 && requestCount.get() > 0 &&
                (errorRequestCount.get() / ((float) requestCount.get())) > (maxErrorRequestRate / 100.0)) {
            // 异常比例超过阈值，打开断路器
            this.isOpen = true;
            return true;
        }

        // 未达到异常阈值，保持关闭状态
        return false;
    }

    /**
     * 重置熔断器，恢复到初始状态
     */
    public void reset() {
        this.isOpen = false;
        this.requestCount.set(0);
        this.errorRequestCount.set(0);
    }

    public static void main(String[] args) {
        // 在这里可以进行熔断器的测试
        CircuitBreaker circuitBreaker = new CircuitBreaker(3, 0.1f);

        new Thread(() -> {
            for (int i = 0; i < 1000; i++) {

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                circuitBreaker.recordRequestCount();
                int num = new Random().nextInt(100);
                if (num > 70) {
                    circuitBreaker.recordErrorRequestCount();
                }

                boolean aBreak = circuitBreaker.isBreak();

                String result = aBreak ? "断路器阻塞了请求" : "断路器放行了请求";

                System.out.println(result);

            }
        }).start();


        new Thread(() -> {
            for (; ; ) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                System.out.println("-----------------------------------------");
                circuitBreaker.reset();
            }
        }).start();

        try {
            Thread.sleep(1000000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
