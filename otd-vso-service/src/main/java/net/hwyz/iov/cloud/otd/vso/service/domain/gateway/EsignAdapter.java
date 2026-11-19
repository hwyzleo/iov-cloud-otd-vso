package net.hwyz.iov.cloud.otd.vso.service.domain.gateway;

/**
 * 电子签适配器（OTD-VSO-DSN-CR-014）
 * <p>
 * 外部电子签渠道统一入口：返回值必须为结构化结果，禁止用字符串拼接承载多个返回值。
 * 回调验签统一由 {@code CallbackSignatureService} 完成，适配器不持有验签职责。
 *
 * @author VSO Team
 */
public interface EsignAdapter {

    /**
     * 生成合同
     *
     * @param orderId      订单业务 ID
     * @param contractType 合同类型
     * @param templateId   模板 ID
     * @return 结构化生成结果（外部合同标识、文件地址、生成时间）
     */
    ContractGenerationResult generateContract(String orderId, String contractType, String templateId);

    /**
     * 发起签署任务
     *
     * @param contractId 合同业务 ID
     * @param signerId   签署人账号 ID
     * @return 结构化签署任务结果（签署任务号、签署地址）
     */
    SignTaskResult createSignTask(String contractId, String signerId);

    /**
     * 查询签署状态
     *
     * @param signNo 签署任务号
     * @return 结构化查询结果（状态 + 当前签署地址）
     */
    EsignSignStatusResult querySignStatus(String signNo);

}
