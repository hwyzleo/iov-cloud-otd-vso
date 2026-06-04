package net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EarnestMoneyOrderRequestVo {

    @NotBlank(message = "销售车型代码不能为空")
    private String saleModelCode;

    @NotBlank(message = "车型代码不能为空")
    private String modelCode;

    @NotBlank(message = "版本代码不能为空")
    private String variantCode;

    private String orderNo;

    @NotEmpty(message = "选项配置不能为空")
    private List<String> optionCodes;

    private String licenseCityCode;

}
