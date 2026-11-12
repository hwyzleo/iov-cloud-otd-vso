package net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 销售车型图片上传完成确认请求（OTD-VSO-DSN-CR-003 §4.2）
 * <p>
 * 凭证放请求体而非 query，避免进入访问日志。
 *
 * @author VSO Team
 */
@Data
public class SaleModelImageCompleteDto {

    /**
     * 申请上传凭证时返回的 uploadToken
     */
    @NotBlank(message = "上传凭证不能为空")
    private String uploadToken;
}
