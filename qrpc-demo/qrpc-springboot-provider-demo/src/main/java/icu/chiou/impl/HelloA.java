package icu.chiou.impl;

import icu.chiou.HelloQRpc;
import icu.chiou.annotation.QRpcApi;

/**
 * Author: chiou
 * createTime: 2023/8/2
 * Description: No Description
 */
@QRpcApi(group = "primary")
public class HelloA implements HelloQRpc {
    @Override
    public String say(String msg) {
        return "🌶️🌶️🌶️🌶️" + msg;
    }
}
