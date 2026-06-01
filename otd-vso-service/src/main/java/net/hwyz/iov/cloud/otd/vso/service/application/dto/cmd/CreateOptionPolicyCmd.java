package net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOptionPolicyCmd {
    private String saleModelCode;

    @NotBlank(message = "Option代码不能为空")
    private String optionCode;

    private String optionFamilyCode;

    @NotBlank(message = "销售状态不能为空")
    private String saleStatus;
    private BigDecimal optionPrice;
    private List<String> availableRegions;
    private List<String> channels;
    private List<String> bundleWith;
    private List<String> mutexWith;
    private String marketingTitle;
    private String marketingImage;
    private Integer sortWeight;
    private String effectiveFrom;
    private String effectiveTo;
}
