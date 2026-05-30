package net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd;

import lombok.Data;
import java.util.List;

@Data
public class CreateOptionFamilyPolicyCmd {
    private String saleModelCode;
    private String optionFamilyCode;
    private List<String> optionFamilyCodes;
    private String marketingTitle;
    private String marketingImage;
    private String marketingDesc;
    private Integer sortWeight;
}
