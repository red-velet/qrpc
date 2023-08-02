package icu.chiou.controller;

import icu.chiou.HelloQRpc;
import icu.chiou.annotation.QRpcService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Author: chiou
 * createTime: 2023/8/2
 * Description: No Description
 */
@RestController
public class HelloController {

    @QRpcService
    private HelloQRpc helloQRpc;

    @RequestMapping("hello")
    public String hello() {
        String res = helloQRpc.say("hello provider,i am consumer");
        return res;
    }
}
