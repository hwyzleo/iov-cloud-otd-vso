package net.hwyz.iov.cloud.otd.vso.service.common.exception;

/**
 * 重复未支付订单异常
 *
 * @author hwyz_leo
 */
public class DuplicateUnpaidOrderException extends VsoBaseException {

    public DuplicateUnpaidOrderException(String accountNo) {
        super(VsoErrorCode.DUPLICATE_UNPAID_ORDER, "账号[" + accountNo + "]存在重复未支付订单");
    }
}
