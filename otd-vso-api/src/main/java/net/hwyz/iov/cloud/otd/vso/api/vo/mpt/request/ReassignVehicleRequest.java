package net.hwyz.iov.cloud.otd.vso.api.vo.mpt.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 换绑VIN请求
 */
@Data
public class ReassignVehicleRequest {

    @NotBlank(message = "订单号不能为空")
    private String orderNo;

    @NotBlank(message = "新VIN不能为空")
    private String newVin;
}