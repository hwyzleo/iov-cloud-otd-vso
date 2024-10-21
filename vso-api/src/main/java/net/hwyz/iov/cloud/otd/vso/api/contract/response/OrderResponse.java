package net.hwyz.iov.cloud.otd.vso.api.contract.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import net.hwyz.iov.cloud.otd.vso.api.contract.SelectedSaleModel;
import net.hwyz.iov.cloud.otd.vso.api.contract.request.SelectedSaleModelRequest;

import java.math.BigDecimal;
import java.util.Date;
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
public class OrderResponse extends SelectedSaleModel {
    /**
     * 订单编号
     */
    private String orderNum;
    /**
     * 订单状态
     */
    private Integer orderState;
    /**
     * 下单时间
     */
    private Date orderTime;
}
