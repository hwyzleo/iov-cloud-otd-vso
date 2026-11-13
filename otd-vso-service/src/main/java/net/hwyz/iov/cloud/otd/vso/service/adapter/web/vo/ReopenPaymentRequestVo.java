package net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 重新发起支付请求（OTD-VSO-DSN-CR-005）
 *
 * @author VSO Team
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ReopenPaymentRequestVo {

    @NotBlank(message = "订单号不能为空")
    private String orderNo;

}
