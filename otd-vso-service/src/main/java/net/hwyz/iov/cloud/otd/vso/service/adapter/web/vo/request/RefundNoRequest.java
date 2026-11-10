package net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 退款单号请求（查询/重试）
 *
 * @author VSO Team
 */
@Data
public class RefundNoRequest {

    @NotBlank(message = "退款单号不能为空")
    private String refundNo;
}
