package icu.chiou;

import icu.chiou.core.annotation.EnableQRpcConsumer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Author: chiou
 * createTime: 2023/8/11
 * Description: No Description
 */
@SpringBootApplication
@EnableQRpcConsumer
public class ConsumerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConsumerApplication.class, args);
    }
}
