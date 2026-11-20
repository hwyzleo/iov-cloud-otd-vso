package net.hwyz.iov.cloud.otd.vso.api.vo.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serializable;

/**
 * 绑车事实查询请求（内部服务调用，OTD-VSO-DSN-CR-017）
 * <p>
 * 只接收可信账号断言与 VIN 两项查询条件；角色、订单状态、交付时间、orderId、目标状态等
 * 均不得由调用方作为可信事实传入。VIN 由服务端去除首尾空白并转大写后按统一规则校验。
 *
 * @author VSO Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BindingFactsQueryRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 平台账号标识（与 vso_order_party.user_id 同一命名空间）
     */
    @NotBlank(message = "账号标识不能为空")
    @Size(max = 64, message = "账号标识长度不能超过 64")
    private String accountId;

    /**
     * 车辆 VIN（服务端规范化后按 17 位、不含 I/O/Q 校验）
     */
    @NotBlank(message = "VIN 不能为空")
    @Size(max = 32, message = "VIN 长度不合法")
    private String vin;
}
