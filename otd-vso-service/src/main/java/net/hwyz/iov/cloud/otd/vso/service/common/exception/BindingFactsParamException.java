package net.hwyz.iov.cloud.otd.vso.service.common.exception;

/**
 * 绑车事实查询参数异常（OTD-VSO-DSN-CR-017）
 * <p>
 * 缺失/非法账号、非法 VIN 等入参问题使用既有 {@code 201001 INVALID_PARAM}，
 * 由 Web 层映射为 HTTP 400；不新增数字错误码，也不得转成 {@code NO_MATCH}。
 *
 * @author VSO Team
 */
public class BindingFactsParamException extends VsoBaseException {

    public BindingFactsParamException(String detail) {
        super(VsoErrorCode.INVALID_PARAM, detail);
    }
}
