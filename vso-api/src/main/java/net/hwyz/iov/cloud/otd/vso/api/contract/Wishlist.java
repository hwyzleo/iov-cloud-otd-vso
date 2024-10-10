package net.hwyz.iov.cloud.otd.vso.api.contract;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Map;

/**
 * 心愿单
 *
 * @author hwyz_leo
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Wishlist {

    /**
     * 销售代码
     */
    @NotEmpty(message = "销售代码不能为空")
    private String saleCode;

    /**
     * 销售车型代码
     */
    @NotEmpty(message = "销售车型代码不能为空")
    private String saleModelCode;

    /**
     * 销售车型类型
     * key: 销售车型类型
     * value: 销售车型类型代码
     */
    Map<String, String> saleModelType;

}
