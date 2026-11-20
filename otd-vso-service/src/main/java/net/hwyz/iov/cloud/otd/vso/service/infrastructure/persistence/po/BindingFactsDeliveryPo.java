package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 绑车事实查询的交付记录投影（OTD-VSO-DSN-CR-017）
 * <p>
 * 只读取交付事实核验必需字段，不含接收人、手机号与授权材料。
 *
 * @author VSO Team
 */
@Data
public class BindingFactsDeliveryPo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 交付记录业务 ID
     */
    private String deliveryRecordId;

    /**
     * 订单业务 ID
     */
    private String orderId;

    /**
     * 交付状态（delivered 表示交付完成）
     */
    private String deliveryStatus;

    /**
     * 实际交付时间
     */
    private LocalDateTime actualDeliveryTime;

    /**
     * 交付 VIN
     */
    private String vehicleVin;

    /**
     * 记录版本
     */
    private Integer rowVersion;
}
