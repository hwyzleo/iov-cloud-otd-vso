package net.hwyz.iov.cloud.otd.vso.api.vo.mpt.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MPT 确认交付请求
 * <p>
 * 仅承载可选交付备注；订单定位、操作人与操作时间均由服务端路径/安全上下文/服务端时间生成。
 *
 * @author hwyz_leo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompleteDeliveryRequest {

    /**
     * 交付备注（可选，最大 255 字符）
     */
    @Size(max = 255, message = "交付备注不能超过 255 字符")
    private String deliveryRemark;

}
