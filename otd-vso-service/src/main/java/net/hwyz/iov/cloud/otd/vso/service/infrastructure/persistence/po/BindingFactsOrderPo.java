package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 绑车事实查询的订单投影（OTD-VSO-DSN-CR-017）
 * <p>
 * 只映射查询必需的最小列，避免整表加载后内存筛选，也不读取客户资料类字段。
 *
 * @author VSO Team
 */
@Data
public class BindingFactsOrderPo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 订单业务 ID
     */
    private String orderId;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 订单状态值
     */
    private Integer orderState;

    /**
     * 当前绑定 VIN
     */
    private String vehicleVin;

    /**
     * 记录版本
     */
    private Integer rowVersion;

    /**
     * 创建时间（不对外输出，仅用于稳定排序）
     */
    private LocalDateTime createTime;
}
