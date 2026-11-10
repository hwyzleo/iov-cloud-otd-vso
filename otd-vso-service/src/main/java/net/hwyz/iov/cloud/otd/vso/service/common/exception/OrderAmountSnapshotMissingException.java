package net.hwyz.iov.cloud.otd.vso.service.common.exception;

/**
 * 订单金额快照缺失异常
 *
 * @author VSO Team
 */
public class OrderAmountSnapshotMissingException extends VsoBaseException {

    public OrderAmountSnapshotMissingException(String message) {
        super(VsoErrorCode.ORDER_AMOUNT_SNAPSHOT_MISSING, message);
    }
}
