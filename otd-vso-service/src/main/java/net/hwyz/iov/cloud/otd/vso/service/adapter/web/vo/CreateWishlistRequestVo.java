package net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

/**
 * 创建心愿单请求 Vo
 *
 * @author VSO Team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateWishlistRequestVo {

    @NotBlank(message = "销售车型代码不能为空")
    private String saleModelCode;

    @NotBlank(message = "Model 编码不能为空")
    private String modelCode;

    @NotBlank(message = "Variant 编码不能为空")
    private String variantCode;

    @NotEmpty(message = "OptionCode 列表不能为空")
    private List<String> optionCodes;

}
