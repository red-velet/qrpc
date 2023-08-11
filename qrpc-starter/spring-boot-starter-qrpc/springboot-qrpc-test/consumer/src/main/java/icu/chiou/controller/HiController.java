package icu.chiou.controller;

import icu.chiou.core.annotation.QRpcService;
import icu.chiou.service.HelloService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Author: chiou
 * createTime: 2023/8/11
 * Description: No Description
 */
@RestController
public class HiController {
    @QRpcService(loadBalancer = "minResponseTime")
    HelloService helloService;


    @RequestMapping("hi")
    public String say() {
        System.out.println(helloService);
        String say = helloService.say("hi 😊😊😊");
        say += "!!!";
        return say;
    }
}
