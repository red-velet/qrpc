package icu.chiou.controller;


import icu.chiou.core.annotation.CircuitBreaker;
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
public class HelloController {
    @QRpcService(loadBalancer = "roundRobin")
    @CircuitBreaker(allowMaxErrorRequest = 20, allErrorRate = 0.5f)
    HelloService helloService;


    @RequestMapping("hello")
    public String say() {
        String say = helloService.say("hello 😊😊😊");
        say += "!!!";
        return say;
    }

    @RequestMapping("hello2")
    public String say2() {
        String say = helloService.say("hello 😊😊😊");
        say += "!!!";
        return say;
    }
}
