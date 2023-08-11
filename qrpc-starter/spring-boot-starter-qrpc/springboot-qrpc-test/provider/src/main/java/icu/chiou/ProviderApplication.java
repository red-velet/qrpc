package icu.chiou;

import icu.chiou.core.annotation.EnableQRpcProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Author: chiou
 * createTime: 2023/8/11
 * Description: No Description
 */
@SpringBootApplication
@EnableQRpcProvider
public class ProviderApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProviderApplication.class, args);
    }
}
