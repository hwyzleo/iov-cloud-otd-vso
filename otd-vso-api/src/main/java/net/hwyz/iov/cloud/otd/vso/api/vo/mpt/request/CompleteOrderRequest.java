package net.hwyz.iov.cloud.otd.vso.api.vo.mpt.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MPT 完成订单请求（OTD-VSO-DSN-CR-016）
 * <p>
 * 仅承载可选办结备注；订单定位、操作人与办结时间均由服务端路径/安全上下文/服务端时间生成，
 * 请求体不得携带操作人、角色、操作来源、目标状态或办结时间。
 *
 * @author hwyz_leo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompleteOrderRequest {

    /**
     * 办结备注（可选，最大 255 字符）
     */
    @Size(max = 255, message = "办结备注不能超过 255 字符")
    private String completionRemark;

}
