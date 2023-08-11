package icu.chiou.protocol.transport;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Author: chiou
 * createTime: 2023/7/26
 * Description: 服务调用方发起的请求内容
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QRpcRequest {
    private long requestId;
    private byte requestType;
    private byte compressType;
    private byte serializeType;
    private RequestPayload requestPayload;
}
