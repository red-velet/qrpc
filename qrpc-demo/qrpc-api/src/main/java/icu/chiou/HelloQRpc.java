package icu.chiou;

/**
 * Author: chiou
 * createTime: 2023/7/23
 * Description: 通用接口,provider和consumer都需要实现
 */
public interface HelloQRpc {
    /**
     * provider和consumer都需要实现的say方法
     *
     * @param msg 交流的信息
     * @return 响应回答
     */
    String say(String msg);
}
