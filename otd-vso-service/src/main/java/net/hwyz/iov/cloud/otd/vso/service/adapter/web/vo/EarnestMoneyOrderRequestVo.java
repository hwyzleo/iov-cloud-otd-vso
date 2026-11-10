package net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    /** 选装代码，允许空数组但元素不可为空且不可重复 */
    @NotNull(message = "选项配置不能为空（可为空数组）")
    private List<String> optionCodes;

    private String licenseCityCode;

    /** 下单确认的购车协议（必填：需覆盖全部当前生效协议） */
    @Valid
    @NotNull(message = "购车协议确认不能为空")
    private List<AgreementConfirmationVo> agreementConfirmations;

}
