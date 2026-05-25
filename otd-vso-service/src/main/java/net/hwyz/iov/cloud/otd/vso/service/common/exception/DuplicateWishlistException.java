package net.hwyz.iov.cloud.otd.vso.service.common.exception;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DuplicateWishlistException extends VsoBaseException {

    public DuplicateWishlistException(String userId, String buildConfigCode) {
        super(ERROR_CODE_DUPLICATE_WISHLIST, "该配置已存在心愿单，请勿重复添加");
        log.warn("用户[{}]已存在相同配置[{}]的心愿单，禁止重复创建", userId, buildConfigCode);
    }
}