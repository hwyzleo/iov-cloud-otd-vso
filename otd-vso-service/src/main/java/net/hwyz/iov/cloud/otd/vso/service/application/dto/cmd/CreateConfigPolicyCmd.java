package net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateConfigPolicyCmd {
    /**
     * 销售车型编码
     */
    private String saleModelCode;

    /**
     * Model 编码
     */
    private String modelCode;

    /**
     * Variant 编码
     */
    private String variantCode;

    /**
     * Configuration 编码列表
     */
    @NotEmpty(message = "Configuration编码列表不能为空")
    private List<String> configurationCodes;

    /**
     * 状态，默认 active
     */
    private String status;
}
