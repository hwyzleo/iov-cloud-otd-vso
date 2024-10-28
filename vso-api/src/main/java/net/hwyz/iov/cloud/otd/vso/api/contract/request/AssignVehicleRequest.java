package net.hwyz.iov.cloud.otd.vso.api.contract.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 分配车辆请求
 *
 * @author hwyz_leo
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AssignVehicleRequest {

    /**
     * 订单号
     */
    private String orderNum;
    /**
     * 车架号
     */
    private String vin;

}
