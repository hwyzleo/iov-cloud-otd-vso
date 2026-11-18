package net.hwyz.iov.cloud.otd.vso.service.application.dto.result;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 待发运订单摘要（补偿通道查询结果，OTD-VSO-DSN-CR-012）
 *
 * @author VSO Team
 */
@Data
@Builder
public class TransportOrderBriefResult {

    private String orderId;
    private String orderNo;
    private Integer orderState;
    private String vin;
    private String deliveryStoreCode;
    private String deliveryRegionCode;
    private LocalDateTime transportApplyTime;
    private String transportApplyPersonName;
}
