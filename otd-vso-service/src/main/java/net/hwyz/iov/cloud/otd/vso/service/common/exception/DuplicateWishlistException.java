package net.hwyz.iov.cloud.otd.vso.service.common.exception;

/**
 * 重复心愿单异常
 *
 * @author hwyz_leo
 */
public class DuplicateWishlistException extends VsoBaseException {

    public DuplicateWishlistException(String accountNo) {
        super(VsoErrorCode.DUPLICATE_WISHLIST, "账号[" + accountNo + "]存在重复心愿单");
    }
}
