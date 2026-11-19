package net.hwyz.iov.cloud.otd.vso.service.common.exception;

/**
 * 合同状态不允许操作或回调状态冲突异常（OTD-VSO-DSN-CR-014）
 *
 * @author VSO Team
 */
public class ContractStatusNotAllowedException extends VsoBaseException {

    public ContractStatusNotAllowedException(String message) {
        super(VsoErrorCode.CONTRACT_STATUS_NOT_ALLOWED, message);
    }
}
