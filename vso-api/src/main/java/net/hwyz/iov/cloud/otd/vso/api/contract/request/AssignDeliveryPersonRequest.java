package net.hwyz.iov.cloud.otd.vso.api.contract.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 分配交付人员请求
 *
 * @author hwyz_leo
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AssignDeliveryPersonRequest {

    /**
     * 订单号
     */
    private String orderNum;
    /**
     * 交付人员ID
     */
    private String deliveryPersonId;
    /**
     * 交付人员名称
     */
    private String deliveryPersonName;

}
