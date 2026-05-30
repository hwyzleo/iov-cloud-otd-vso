package net.hwyz.iov.cloud.otd.vso.service.common.exception;

/**
 * 销售车型 Variant 锁定异常
 * 当 SaleModel 已有活跃订单或心愿单时，不允许修改 variantCode
 */
public class SaleModelVariantLockedException extends VsoBaseException {

    public SaleModelVariantLockedException(String message) {
        super(VsoErrorCode.SALE_MODEL_VARIANT_LOCKED, message);
    }
}
