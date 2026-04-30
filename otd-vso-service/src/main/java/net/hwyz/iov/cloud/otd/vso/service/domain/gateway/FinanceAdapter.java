package net.hwyz.iov.cloud.otd.vso.service.domain.gateway;

/**
 * 金融适配器
 *
 * @author VSO Team
 */
public interface FinanceAdapter {

    /**
     * 提交金融申请
     *
     * @param orderId 订单 ID
     * @param financeType 金融方案类型
     * @param applicantId 申请人 ID
     * @return 申请单号
     */
    String applyFinance(String orderId, String financeType, String applicantId);

    /**
     * 查询金融申请状态
     *
     * @param applyNo 申请单号
     * @return 申请状态
     */
    String queryFinanceStatus(String applyNo);

    /**
     * 验证回调签名
     *
     * @param payload 回调报文
     * @param signature 签名
     * @return 是否有效
     */
    boolean verifyCallbackSignature(String payload, String signature);

}
