package net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo;

import lombok.Data;

/**
 * 销售车型图片上传凭证响应（OTD-VSO-DSN-CR-003 §4.1）
 * <p>
 * 不下发 objectKey，减少内部键暴露；uploadToken 用于完成确认时绑定归属校验。
 *
 * @author VSO Team
 */
@Data
public class SaleModelImageTicketVo {

    /**
     * 上传任务标识
     */
    private String objectId;

    /**
     * COS PUT 预签名地址
     */
    private String uploadUrl;

    /**
     * 上传方法，固定 PUT
     */
    private String httpMethod;

    /**
     * 必须原样携带的 Content-Type
     */
    private String contentType;

    /**
     * 预签名过期时间（epoch 毫秒）
     */
    private Long expiresAt;

    /**
     * 上传凭证，完成确认时必须原样提交
     */
    private String uploadToken;
}
