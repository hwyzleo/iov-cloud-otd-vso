package net.hwyz.iov.cloud.otd.vso.service.common.exception;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DuplicateUnpaidOrderException extends VsoBaseException {

    private final String checkDimension;

    public DuplicateUnpaidOrderException(String userId) {
        super(ERROR_CODE_DUPLICATE_UNPAID_ORDER, "您有未完成的订单，请先完成支付或取消后再下单");
        this.checkDimension = "userId";
        log.warn("用户[{}]存在未完成订单，禁止重复下单", userId);
    }

    public DuplicateUnpaidOrderException(String mobileHash, boolean isMobile) {
        super(ERROR_CODE_DUPLICATE_UNPAID_ORDER, "该手机号存在未完成的订单，请先完成支付或取消后再下单");
        this.checkDimension = "mobileHash";
        log.warn("手机号[{}]存在未完成订单，禁止重复下单", mobileHash);
    }

    public String getCheckDimension() {
        return checkDimension;
    }
}