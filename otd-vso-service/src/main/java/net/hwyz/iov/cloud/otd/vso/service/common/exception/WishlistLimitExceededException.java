package net.hwyz.iov.cloud.otd.vso.service.common.exception;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WishlistLimitExceededException extends VsoBaseException {

    public static final int WISHLIST_LIMIT = 5;

    public WishlistLimitExceededException(String userId) {
        super(ERROR_CODE_WISHLIST_LIMIT_EXCEEDED, "心愿单已达上限（5个），请删除后再创建");
        log.warn("用户[{}]心愿单已达上限{}，禁止继续创建", userId, WISHLIST_LIMIT);
    }
}