package net.hwyz.iov.cloud.otd.vso.service.application.dto.event;

import lombok.Builder;
import lombok.Getter;

/**
 * 发运申请事件（VSO → TMS，OTD-VSO-DSN-CR-012）
 * <p>
 * Kafka Key = orderNo；载荷为提交时刻快照，只含履约必需字段，不含客户个人信息。
 *
 * @author VSO Team
 */
@Getter
@Builder
public class TransportAppliedEvent {

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
     * 发运申请载荷（履约必需字段）
     */
    @Getter
    @Builder
    public static class Payload {

        /**
         * 订单号（业务幂等键）
         */
        private String orderNo;

        /**
         * 订单业务 ID
         */
        private String orderId;

        /**
         * 订单状态值（470 发运申请）
         */
        private Integer orderState;

        /**
         * 交付 VIN
         */
        private String vin;

        /**
         * 品牌代码
         */
        private String brandCode;

        /**
         * 销售车型
         */
        private String saleModel;

        /**
         * 配置代码
         */
        private String configurationCode;

        /**
         * 交付门店代码
         */
        private String deliveryStoreCode;

        /**
         * 交付区域代码
         */
        private String deliveryRegionCode;

        /**
         * 上牌城市
         */
        private String licenseCity;

        /**
         * 发运申请人 ID
         */
        private String transportApplyPersonId;

        /**
         * 发运申请人姓名
         */
        private String transportApplyPersonName;

        /**
         * 发运申请时间（ISO-8601）
         */
        private String transportApplyTime;
    }
}
