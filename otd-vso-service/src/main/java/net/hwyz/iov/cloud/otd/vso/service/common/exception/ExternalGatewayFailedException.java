package net.hwyz.iov.cloud.otd.vso.service.common.exception;

/**
 * 外部网关调用失败异常
 *
 * @author VSO Team
 */
public class ExternalGatewayFailedException extends VsoBaseException {

    public ExternalGatewayFailedException(String message) {
        super(VsoErrorCode.EXTERNAL_GATEWAY_FAILED, message);
    }
}
