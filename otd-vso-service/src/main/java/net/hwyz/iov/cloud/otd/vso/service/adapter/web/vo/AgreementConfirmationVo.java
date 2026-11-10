package net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * 购车协议确认参数
 *
 * @author VSO Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgreementConfirmationVo {

    @NotBlank(message = "协议代码不能为空")
    @Size(max = 64, message = "协议代码最长 64")
    private String agreementCode;

    @NotNull(message = "协议版本号不能为空")
    @Min(value = 1, message = "协议版本号必须大于等于 1")
    private Integer versionNo;
}
