package net.hwyz.iov.cloud.otd.vso.service.common.exception;

/**
 * 销售型号配置已锁定异常
 *
 * @author hwyz_leo
 */
public class SaleModelConfigHasLockedException extends VsoBaseException {

    public SaleModelConfigHasLockedException(String configCode) {
        super(VsoErrorCode.CONFIGURATION_HAS_LOCKED, "订单配置[" + configCode + "]已锁定");
    }
}
