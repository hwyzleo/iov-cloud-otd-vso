package net.hwyz.iov.cloud.otd.vso.service.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.framework.common.util.StrUtil;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.event.OrderDeliveredEvent;
import net.hwyz.iov.cloud.otd.vso.service.application.port.EventPayloadSerializer;
import net.hwyz.iov.cloud.otd.vso.service.common.time.BusinessTimeZone;
import net.hwyz.iov.cloud.otd.vso.service.common.util.VsoEventTime;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.DeliveryRecord;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Order;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderOutboxRecord;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderOutboxRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.config.VsoOutboxProperties;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;

/**
 * 交付履约应用服务（OTD-VSO-DSN-CR-016）
 * <p>
 * 只负责把交付事实写入本地发件箱（与订单交付同一事务）；不发布 Kafka（发布由既有中继完成），
 * 不改变订单状态机，不声明任何承接服务或消费者。
 *
 * @author VSO Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryFulfillmentAppService {

    /**
     * 事件类型：订单已交付
     */
    public static final String EVENT_TYPE_ORDER_DELIVERED = "vso.order.delivered";

    /**
     * 载荷版本
     */
    private static final int SCHEMA_VERSION = 1;

    /**
     * 生产者标识
     */
    private static final String PRODUCER = "otd-vso";

    /**
     * 初始发布状态
     */
    private static final String PUBLISH_STATE_PENDING = "PENDING";

    private final OrderOutboxRepository orderOutboxRepository;
    private final EventPayloadSerializer eventPayloadSerializer;
    private final VsoOutboxProperties outboxProperties;
    private final BusinessTimeZone businessTimeZone;

    /**
     * 追加交付事实事件到发件箱
     * <p>
     * 必须在调用方（订单交付）的本地事务内执行：事务回滚时不得留下发件箱记录。
     * 同一订单重复触发时按「已存在生效事件」跳过；必要标识缺失时拒绝入箱，不发布伪事实。
     *
     * @param order          已流转到 650 的订单
     * @param deliveryRecord 同事务写入的真实交付记录
     */
    public void appendDelivered(Order order, DeliveryRecord deliveryRecord) {
        if (order == null || StrUtil.isBlank(order.getOrderNo()) || StrUtil.isBlank(order.getId())) {
            log.warn("交付事件入箱跳过：订单标识缺失");
            return;
        }
        if (deliveryRecord == null || StrUtil.isBlank(deliveryRecord.getDeliveryRecordId())) {
            log.warn("交付事件入箱跳过：交付记录标识缺失，orderNo={}", order.getOrderNo());
            return;
        }
        if (orderOutboxRepository.existsByEventTypeAndAggregateId(EVENT_TYPE_ORDER_DELIVERED, order.getId())) {
            log.info("订单[{}]已存在交付事件，跳过重复入箱", order.getOrderNo());
            return;
        }
        ZoneId zone = businessTimeZone.zoneId();
        Instant occurredAt = VsoEventTime.now();
        OrderDeliveredEvent event = OrderDeliveredEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(EVENT_TYPE_ORDER_DELIVERED)
                .schemaVersion(SCHEMA_VERSION)
                .occurredAt(VsoEventTime.format(occurredAt, zone))
                .producer(PRODUCER)
                .aggregateVersion(order.getCurrentVersionNo() == null
                        ? 0L : order.getCurrentVersionNo().longValue())
                .payload(OrderDeliveredEvent.Payload.builder()
                        .orderId(order.getId())
                        .orderNo(order.getOrderNo())
                        .orderState(order.getOrderState() == null ? null : order.getOrderState().getValue())
                        .vin(deliveryRecord.getVehicleVin())
                        .deliveryRecordId(deliveryRecord.getDeliveryRecordId())
                        .deliveryStoreCode(deliveryRecord.getDeliveryStoreCode())
                        .actualDeliveryTime(VsoEventTime.format(
                                businessTimeZone.toInstant(deliveryRecord.getActualDeliveryTime()), zone))
                        .build())
                .build();
        orderOutboxRepository.insert(OrderOutboxRecord.builder()
                .eventId(event.getEventId())
                .eventType(EVENT_TYPE_ORDER_DELIVERED)
                .aggregateType(outboxProperties.getAggregateType())
                .aggregateId(order.getId())
                .aggregateVersion(event.getAggregateVersion())
                .topic(outboxProperties.getTopics().getOrderDelivered())
                .messageKey(order.getOrderNo())
                .payload(eventPayloadSerializer.serialize(event))
                .publishState(PUBLISH_STATE_PENDING)
                .retryCount(0)
                .build());
        log.info("交付事件已入箱：orderNo={}, eventId={}, topic={}", order.getOrderNo(), event.getEventId(),
                outboxProperties.getTopics().getOrderDelivered());
    }
}
