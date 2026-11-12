package net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd;

import lombok.Builder;
import lombok.Data;

/**
 * 销售车型图片上传凭证申请命令（OTD-VSO-DSN-CR-003 §4.1）
 *
 * @author VSO Team
 */
@Data
@Builder
public class GenerateSaleModelImageUploadCmd {

    /**
     * 原始文件名
     */
    private String originalFilename;

    /**
     * 声明 MIME
     */
    private String contentType;

    /**
     * 声明大小（字节）
     */
    private Long size;

    /**
     * 操作人 ID（来自安全上下文）
     */
    private String operatorId;
}
