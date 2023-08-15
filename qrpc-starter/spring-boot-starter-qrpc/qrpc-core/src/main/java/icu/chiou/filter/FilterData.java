package icu.chiou.filter;

import icu.chiou.protocol.transport.QRpcResponse;
import icu.chiou.protocol.transport.RequestPayload;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * Author: chiou
 * createTime: 2023/8/14
 * Description: No Description
 */
@Setter
@Getter
public class FilterData {
    private String interfaceName;

    private String methodName;

    private Class<?>[] paramsType;
    private Object[] paramsValue;

    private Class<?> returnType;

    private Map<String, Object> providerAttributes;
    private Map<String, Object> consumerAttributes;


    private long requestId;
    private byte compressType;
    private byte serializeType;
    private byte code;
    private Object body;

    public FilterData() {
    }

    public void setBody(Object body) {
        this.body = body;
    }

    public FilterData(RequestPayload payload) {
        this.interfaceName = payload.getInterfaceName();
        this.methodName = payload.getInterfaceName();
        this.paramsType = payload.getParamsType();
        this.paramsValue = payload.getParamsValue();
        this.returnType = payload.getReturnType();
        this.providerAttributes = payload.getProviderAttributes();
        this.consumerAttributes = payload.getConsumerAttributes();
    }

    public FilterData(QRpcResponse resp) {
        this.requestId = resp.getRequestId();
        this.compressType = resp.getCompressType();
        this.serializeType = resp.getSerializeType();
        this.code = resp.getCode();
        this.body = resp.getBody();
    }

    public FilterData(byte code, Object o) {
        this.code = code;
        this.body = o;
    }

    public FilterData(byte code) {
        this.code = code;
    }

    public FilterData(long requestId, byte compressType, byte serializeType, byte code, Object body) {
        this.requestId = requestId;
        this.compressType = compressType;
        this.serializeType = serializeType;
        this.code = code;
        this.body = body;
    }
}
