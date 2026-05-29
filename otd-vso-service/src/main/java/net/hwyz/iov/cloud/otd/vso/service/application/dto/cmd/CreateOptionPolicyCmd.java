package net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class CreateOptionPolicyCmd {
    private String saleModelCode;
    private String optionCode;
    private String optionFamilyCode;
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
