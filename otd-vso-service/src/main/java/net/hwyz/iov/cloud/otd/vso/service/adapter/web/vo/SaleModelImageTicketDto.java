package net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 销售车型图片上传凭证申请请求（OTD-VSO-DSN-CR-003 §4.1）
 * <p>
 * 申请阶段由 VSO 前置校验，避免无效请求打到 EDD-OSS；最终以 OSS 对 COS 实际对象的核验结果为准。
 *
 * @author VSO Team
 */
@Data
public class SaleModelImageTicketDto {

    /**
     * 原始文件名，去空白后非空且不超过 255 字符
     */
    @NotBlank(message = "原始文件名不能为空")
    @Size(max = 255, message = "原始文件名长度不能超过 255 字符")
    private String originalFilename;

    /**
     * 声明 MIME，仅 image/jpeg、image/png、image/webp
     */
    @NotBlank(message = "图片 MIME 类型不能为空")
    @Pattern(regexp = "image/(jpeg|png|webp)", message = "仅支持 image/jpeg、image/png、image/webp")
    private String contentType;

    /**
     * 声明大小，1 ≤ size ≤ 10 MiB
     */
    @NotNull(message = "图片大小不能为空")
    @Min(value = 1, message = "图片大小必须大于 0")
    @Max(value = 10485760, message = "图片大小不能超过 10 MiB")
    private Long size;
}
