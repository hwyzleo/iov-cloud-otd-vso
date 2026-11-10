package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po;

import lombok.Data;

/**
 * 交付人员订单数统计（批量查询投影）
 *
 * @author VSO Team
 */
@Data
public class DeliveryPersonCountPo {

    /**
     * 交付人员 ID
     */
    private String deliveryPersonId;

    /**
     * 未交付订单数
     */
    private Integer orderCount;
}
