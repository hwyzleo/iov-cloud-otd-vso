package net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

/**
 * 创建/更新 Model 销售策略命令
 *
 * @author hwyz_leo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateModelPolicyCmd {

    private String saleModelCode;

    @NotBlank(message = "Model代码不能为空")
    private String modelCode;

    @NotBlank(message = "销售状态不能为空")
    private String saleStatus;
    private List<String> availableRegions;
    private List<String> channels;
    private String marketingName;
    private String marketingImage;
    private String marketingCopy;
    private Integer sortWeight;
    private Timestamp effectiveFrom;
    private Timestamp effectiveTo;
}
