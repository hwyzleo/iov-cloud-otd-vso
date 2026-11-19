package net.hwyz.iov.cloud.otd.vso.service.application.dto.event;

import lombok.Builder;
import lombok.Getter;

/**
 * 交付事实事件（VSO → Kafka，OTD-VSO-DSN-CR-016）
 * <p>
 * Kafka Key = orderNo；载荷取自真实交付记录与订单快照，只含履约必需字段，
 * 不含 accountId、订购人、交付人员、备注或其他非必要信息。
 *
 * @author VSO Team
 */
@Getter
@Builder
public class OrderDeliveredEvent {

    /**
     * 事件唯一 ID
     */
    private String eventId;

    /**
     * 事件类型
     */
    private String eventType;

    /**
     * 载荷版本
     */
    private Integer schemaVersion;

    /**
     * 事件发生时间（ISO-8601）
     */
    private String occurredAt;

    /**
     * 生产者标识
     */
    private String producer;

    /**
     * 聚合版本
     */
    private Long aggregateVersion;

    /**
     * 载荷
     */
    private Payload payload;

    /**
     * 交付事实载荷（履约必需字段）
     */
    @Getter
    @Builder
    public static class Payload {

        /**
         * 订单业务 ID
         */
        private String orderId;

        /**
         * 订单号（业务幂等键）
         */
        private String orderNo;

        /**
         * 订单状态值（650 已交付）
         */
        private Integer orderState;

        /**
         * 交付 VIN
         */
        private String vin;

        /**
         * 交付记录业务 ID
         */
        private String deliveryRecordId;

        /**
         * 交付门店代码
         */
        private String deliveryStoreCode;

        /**
         * 实际交付时间（ISO-8601）
         */
        private String actualDeliveryTime;
    }
}
