package net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 电子签回调请求（OTD-VSO-DSN-CR-014）
 * <p>
 * 原实现接收任意 Map 并直接返回成功；现改为强类型并委托 ContractCallbackService。
 *
 * @author VSO Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractCallbackRequest {

    @NotBlank(message = "合同号不能为空")
    private String contractNo;

    @NotBlank(message = "签署任务号不能为空")
    private String signNo;

    @NotBlank(message = "合同回调状态不能为空")
    private String status;

    @NotNull(message = "事件发生时间不能为空")
    private Instant occurredAt;

    @NotBlank(message = "幂等键不能为空")
    private String idempotentKey;

    @NotNull(message = "时间戳不能为空")
    private Long timestamp;

    @NotBlank(message = "随机串不能为空")
    private String nonce;

    /** 失效原因（INVALID 时可选） */
    private String failReason;
}
