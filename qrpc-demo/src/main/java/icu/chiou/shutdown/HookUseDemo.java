package icu.chiou.shutdown;

/**
 * Author: chiou
 * createTime: 2023/8/2
 * Description: No Description
 */
public class HookUseDemo {
    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("程序正在关闭...");
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println("程序已关闭");
        }));
        while (true) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println("程序运转中..");
        }
    }
}
