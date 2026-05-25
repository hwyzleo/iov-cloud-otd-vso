package net.hwyz.iov.cloud.otd.vso.service.common.exception;

/**
 * 心愿单数量超限异常
 *
 * @author hwyz_leo
 */
public class WishlistLimitExceededException extends VsoBaseException {

    public WishlistLimitExceededException(int maxCount) {
        super(VsoErrorCode.WISHLIST_LIMIT_EXCEEDED, "心愿单数量已超限，最大数量: " + maxCount);
    }
}
