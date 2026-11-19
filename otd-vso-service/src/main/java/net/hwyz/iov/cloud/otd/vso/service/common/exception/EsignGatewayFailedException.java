package net.hwyz.iov.cloud.otd.vso.service.common.exception;

/**
 * 电子签网关调用失败异常（OTD-VSO-DSN-CR-014）
 *
 * @author VSO Team
 */
public class EsignGatewayFailedException extends VsoBaseException {

    public EsignGatewayFailedException(String message) {
        super(VsoErrorCode.ESIGN_GATEWAY_FAILED, message);
    }
}
