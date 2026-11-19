package net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 发起合同签署请求（OTD-VSO-DSN-CR-014）
 *
 * @author VSO Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InitiateContractSignRequest {

    @NotBlank(message = "订单号不能为空")
    private String orderNo;
}
