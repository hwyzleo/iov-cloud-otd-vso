package net.hwyz.iov.cloud.otd.vso.service.common.exception;

/**
 * 购车合同不存在异常（OTD-VSO-DSN-CR-014）
 *
 * @author VSO Team
 */
public class ContractNotExistException extends VsoBaseException {

    public ContractNotExistException(String orderNo) {
        super(VsoErrorCode.CONTRACT_NOT_EXIST, "购车合同不存在：" + orderNo);
    }
}
