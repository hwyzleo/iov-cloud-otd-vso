package net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 支付单查询请求
 *
 * @author VSO Team
 */
@Data
public class PaymentQueryRequest {

    @NotBlank(message = "订单号不能为空")
    private String orderNo;

    @NotBlank(message = "支付单号不能为空")
    private String paymentNo;
}
