package net.hwyz.iov.cloud.otd.vso.api.vo.mpt.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 解绑VIN请求
 */
@Data
public class UnbindVehicleRequest {

    @NotBlank(message = "订单号不能为空")
    private String orderNo;

    @NotBlank(message = "解绑原因不能为空")
    private String unbindReason;
}