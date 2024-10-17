package net.hwyz.iov.cloud.otd.vso.api.contract.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import net.hwyz.iov.cloud.otd.vso.api.contract.request.SelectedSaleModelRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 订单详情响应
 *
 * @author hwyz_leo
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OrderResponse extends SelectedSaleModelRequest {
    /**
     * 订单状态
     */
    private Integer orderState;
    /**
     * 销售车型配置名称
     */
    private Map<String, String> saleModelConfigName;
    /**
     * 销售车型配置价格
     */
    private Map<String, BigDecimal> saleModelConfigPrice;
    /**
     * 销售车型图片
     */
    private List<String> saleModelImages;
    /**
     * 总价格
     */
    private BigDecimal totalPrice;
    /**
     * 是否有效
     */
    private Boolean isValid;
}
