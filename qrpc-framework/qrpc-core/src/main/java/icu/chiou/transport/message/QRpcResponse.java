package icu.chiou.transport.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Author: chiou
 * createTime: 2023/7/27
 * Description: 服务提供方发起的响应内容
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QRpcResponse implements Serializable {
    private long requestId;
    private byte compressType;
    private byte serializeType;
    private byte code;
    private Object body;
}
