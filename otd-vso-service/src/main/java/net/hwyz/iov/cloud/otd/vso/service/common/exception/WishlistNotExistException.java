package net.hwyz.iov.cloud.otd.vso.service.common.exception;

/**
 * 心愿单不存在异常
 *
 * @author VSO Team
 */
public class WishlistNotExistException extends VsoBaseException {

    public WishlistNotExistException(String wishlistId) {
        super(ERROR_CODE_WISHLIST_NOT_EXIST, "心愿单不存在：" + wishlistId);
    }

}