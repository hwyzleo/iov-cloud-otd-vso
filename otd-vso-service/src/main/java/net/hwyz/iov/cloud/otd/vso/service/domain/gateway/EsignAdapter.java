package net.hwyz.iov.cloud.otd.vso.service.domain.gateway;

/**
 * 电子签适配器
 *
 * @author VSO Team
 */
public interface EsignAdapter {

    /**
     * 生成合同
     *
     * @param orderId 订单 ID
     * @param contractType 合同类型
     * @param templateId 模板 ID
     * @return 合同文件 ID
     */
    String generateContract(String orderId, String contractType, String templateId);

    /**
     * 发起签署
     *
     * @param contractId 合同 ID
     * @param signerId 签署人 ID
     * @return 签署单号
     */
    String createSignTask(String contractId, String signerId);

    /**
     * 查询签署状态
     *
     * @param signNo 签署单号
     * @return 签署状态
     */
    String querySignStatus(String signNo);

    /**
     * 验证回调签名
     *
     * @param payload 回调报文
     * @param signature 签名
     * @return 是否有效
     */
    boolean verifyCallbackSignature(String payload, String signature);

}
