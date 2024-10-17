package net.hwyz.iov.cloud.otd.vso.api.contract.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Map;

/**
 * 已选择的销售车型请求
 *
 * @author hwyz_leo
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class SelectedSaleModelRequest {

    /**
     * 销售代码
     */
    @NotBlank(message = "销售代码不能为空")
    private String saleCode;

    /**
     * 订单编号
     */
    private String orderNum;

    /**
     * 销售车型配置类型
     * key: 销售车型配置类型
     * value: 销售车型配置类型代码
     */
    @NotEmpty(message = "销售车型配置类型不能为空")
    Map<String, String> saleModelConfigType;

}
