package net.hwyz.iov.cloud.otd.vso.api.vo.response;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 待发运订单摘要（内部服务调用，OTD-VSO-DSN-CR-012）
 * <p>
 * 只包含履约必需字段，不含手机号、证件号、客户姓名与地址等个人信息。
 *
 * @author VSO Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransportOrderBriefResponse implements Serializable {

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
     * 交付 VIN
     */
    private String vin;

    /**
     * 交付门店代码
     */
    private String deliveryStoreCode;

    /**
     * 交付区域代码
     */
    private String deliveryRegionCode;

    /**
     * 发运申请时间
     */
    private LocalDateTime transportApplyTime;

    /**
     * 发运申请人姓名
     */
    private String transportApplyPersonName;
}
