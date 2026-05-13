package net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EarnestMoneyOrderRequestVo {

    @NotBlank(message = "销售车型代码不能为空")
    private String saleModelCode;

    private String orderNo;

    @NotEmpty(message = "特征配置不能为空")
    private Map<String, String> saleModelConfigType;

    @NotBlank(message = "区域代码不能为空")
    private String regionCode;

    private String licenseCityCode;

}
