package net.hwyz.iov.cloud.otd.vso.service.common.exception;

/**
 * 心愿单不存在异常
 *
 * @author hwyz_leo
 */
public class WishlistNotExistException extends VsoBaseException {

    public WishlistNotExistException(String wishlistId) {
        super(VsoErrorCode.WISHLIST_NOT_EXIST, "心愿单[" + wishlistId + "]不存在");
    }
}
