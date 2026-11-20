package net.hwyz.iov.cloud.otd.vso.service.application.dto.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 绑车事实查询条件（OTD-VSO-DSN-CR-017）
 * <p>
 * 只承载账号断言与 VIN 两项查询事实，不接收角色、状态、交付时间或目标状态等调用方自报结论。
 *
 * @author VSO Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BindingFactsQuery {

    /**
     * 平台账号标识
     */
    private String accountId;

    /**
     * 车辆 VIN（入参形态，由应用服务规范化）
     */
    private String vin;
}
