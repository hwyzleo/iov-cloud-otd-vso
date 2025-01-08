package net.hwyz.iov.cloud.otd.vso.api.contract.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 申请发运请求
 *
 * @author hwyz_leo
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ApplyTransportRequest {

    /**
     * 订单号
     */
    private String orderNum;

}
