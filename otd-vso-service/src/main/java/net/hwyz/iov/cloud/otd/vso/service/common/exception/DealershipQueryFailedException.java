package net.hwyz.iov.cloud.otd.vso.service.common.exception;

/**
 * 门店查询失败异常（OTD-VSO-DSN-CR-008 §3/§5.2）
 * <p>
 * 用于 EDD-ORG 调用异常、超时或降级返回 null 的场景；禁止把上游故障解释为“无门店”并返回空列表。
 *
 * @author VSO Team
 */
public class DealershipQueryFailedException extends VsoBaseException {

    public DealershipQueryFailedException(String message) {
        super(VsoErrorCode.DEALERSHIP_QUERY_FAILED, message);
    }

    public DealershipQueryFailedException(String message, Throwable cause) {
        super(VsoErrorCode.DEALERSHIP_QUERY_FAILED, cause);
    }
}
