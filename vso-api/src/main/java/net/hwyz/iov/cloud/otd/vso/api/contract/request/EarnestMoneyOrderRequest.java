package net.hwyz.iov.cloud.otd.vso.api.contract.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 意向金下单请求
 *
 * @author hwyz_leo
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class EarnestMoneyOrderRequest extends SelectedSaleModelRequest {

    /**
     * 上牌城市
     */
    @NotBlank(message = "上牌城市不能为空")
    private String licenseCity;

}
