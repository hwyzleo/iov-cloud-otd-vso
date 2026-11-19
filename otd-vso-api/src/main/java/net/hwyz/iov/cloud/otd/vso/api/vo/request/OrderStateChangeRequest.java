package net.hwyz.iov.cloud.otd.vso.api.vo.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.io.Serializable;

/**
 * 订单状态推进请求（内部服务调用）
 * <p>
 * 4 个状态回写动作（准备运输／运输中／待交付／已交付）共用同一入参模型，
 * 只携带订单号；状态机校验由 VSO 侧完成（OTD-VSO-DSN-CR-012 / OTD-VSO-DSN-CR-016 删除已激活动作）。
 *
 * @author VSO Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStateChangeRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 订单号
     */
    @NotBlank(message = "订单号不能为空")
    private String orderNo;
}
