package net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo;

import lombok.Data;

/**
 * 销售车型图片上传完成确认响应（OTD-VSO-DSN-CR-003 §4.2）
 * <p>
 * accessUrl 为可直接写入 icon/images 的公开访问地址，仅在对象状态达到 UPLOADED 时返回。
 *
 * @author VSO Team
 */
@Data
public class SaleModelImageResultVo {

    /**
     * 上传任务标识
     */
    private String objectId;

    /**
     * 公开访问地址
     */
    private String accessUrl;

    /**
     * 实际 MIME
     */
    private String contentType;

    /**
     * 实际字节数
     */
    private Long size;

    /**
     * 对象状态，正常为 UPLOADED
     */
    private String status;
}
