package icu.chiou.transport.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Author: chiou
 * createTime: 2023/7/26
 * Description:用来描述请求调用方所请求接口方法的描述
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestPayload implements Serializable {
    //1.接口的名字---icu.chiou.HelloQRpc
    private String interfaceName;

    //2.方法的名字---say
    private String methodName;

    //3.参数列表：参数类型、参数值
    private Class<?>[] paramsType;//java.lang.String
    private Object[] paramsValue;//hello

    //4.返回值
    private Class<?> returnType;//java.lang.String
}
