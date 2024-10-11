package net.hwyz.iov.cloud.otd.vso.api.contract.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.hwyz.iov.cloud.otd.vso.api.contract.SaleModelConfig;

import java.util.List;

/**
 * 销售车型返回
 *
 * @author hwyz_leo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleModelResponse {

    /**
     * 销售车型列表
     */
    private List<SaleModelConfig> saleModels;

}
