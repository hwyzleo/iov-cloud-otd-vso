package net.hwyz.iov.cloud.otd.vso.service.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.framework.common.util.StrUtil;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.event.TransportAppliedEvent;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.TransportOrderBriefResult;
import net.hwyz.iov.cloud.otd.vso.service.application.port.EventPayloadSerializer;
import net.hwyz.iov.cloud.otd.vso.service.common.time.BusinessTimeZone;
import net.hwyz.iov.cloud.otd.vso.service.common.util.VsoEventTime;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Order;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderOutboxRecord;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderState;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderOutboxRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.config.VsoOutboxProperties;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 发运履约应用服务（OTD-VSO-DSN-CR-012）
 * <p>
 * 负责两件事：一、把发运申请事实写入本地发件箱（与订单状态同一事务）；二、提供待发运订单只读查询作为补偿通道。
 * 本服务不发布 Kafka（发布由中继完成），不改变订单状态机。
 *
 * @author VSO Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransportFulfillmentAppService {

    /**
     * 事件类型：发运申请
     */
    public static final String EVENT_TYPE_TRANSPORT_APPLIED = "vso.transport.applied";

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
    private final OrderRepository orderRepository;
    private final EventPayloadSerializer eventPayloadSerializer;
    private final VsoOutboxProperties outboxProperties;
    private final BusinessTimeZone businessTimeZone;

    /**
     * 追加发运申请事件到发件箱
     * <p>
     * 必须在调用方（订单状态变更）的本地事务内执行：事务回滚时不得留下发件箱记录（INV-030）。
     * 同一订单重复触发时按「已存在生效事件」跳过，避免产生第二条业务事实。
     *
     * @param order 已流转到 470 的订单
     */
    public void appendTransportApplied(Order order) {
        if (order == null || StrUtil.isBlank(order.getOrderNo()) || StrUtil.isBlank(order.getId())) {
            log.warn("发运申请事件入箱跳过：订单标识缺失");
            return;
        }
        if (orderOutboxRepository.existsByEventTypeAndAggregateId(EVENT_TYPE_TRANSPORT_APPLIED, order.getId())) {
            log.info("订单[{}]已存在发运申请事件，跳过重复入箱", order.getOrderNo());
            return;
        }
        ZoneId zone = businessTimeZone.zoneId();
        Instant occurredAt = VsoEventTime.now();
        TransportAppliedEvent event = TransportAppliedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(EVENT_TYPE_TRANSPORT_APPLIED)
                .schemaVersion(SCHEMA_VERSION)
                .occurredAt(VsoEventTime.format(occurredAt, zone))
                .producer(PRODUCER)
                .aggregateVersion(order.getCurrentVersionNo() == null
                        ? 0L : order.getCurrentVersionNo().longValue())
                .payload(TransportAppliedEvent.Payload.builder()
                        .orderNo(order.getOrderNo())
                        .orderId(order.getId())
                        .orderState(order.getOrderState() == null ? null : order.getOrderState().getValue())
                        .vin(order.getDeliveryVin())
                        .brandCode(order.getBrandCode())
                        .saleModel(order.getSaleModel())
                        .configurationCode(order.getConfigurationCode())
                        .deliveryStoreCode(order.getDeliveryStoreCode())
                        .deliveryRegionCode(order.getDeliveryRegionCode())
                        .licenseCity(order.getLicenseCity())
                        .transportApplyPersonId(order.getTransportApplyPersonId())
                        .transportApplyPersonName(order.getTransportApplyPersonName())
                        .transportApplyTime(VsoEventTime.format(order.getTransportApplyTime(), zone))
                        .build())
                .build();
        orderOutboxRepository.insert(OrderOutboxRecord.builder()
                .eventId(event.getEventId())
                .eventType(EVENT_TYPE_TRANSPORT_APPLIED)
                .aggregateType(outboxProperties.getAggregateType())
                .aggregateId(order.getId())
                .aggregateVersion(event.getAggregateVersion())
                .topic(outboxProperties.getTopics().getTransportApplied())
                .messageKey(order.getOrderNo())
                .payload(eventPayloadSerializer.serialize(event))
                .publishState(PUBLISH_STATE_PENDING)
                .retryCount(0)
                .build());
        log.info("发运申请事件已入箱：orderNo={}, eventId={}, topic={}", order.getOrderNo(), event.getEventId(),
                outboxProperties.getTopics().getTransportApplied());
    }

    /**
     * 待发运订单查询（只读，补偿通道）
     *
     * @param orderStateValue    订单状态值（必填）
     * @param deliveryRegionCode 交付区域代码（可选）
     * @return 订单摘要列表（数据库分页由调用方 PageHelper 完成）
     */
    public List<TransportOrderBriefResult> searchTransportApplied(Integer orderStateValue, String deliveryRegionCode) {
        OrderState orderState = orderStateValue == null ? null : OrderState.valOf(orderStateValue);
        if (orderState == null) {
            throw new IllegalArgumentException("订单状态非法: " + orderStateValue);
        }
        return orderRepository.findTransportApplied(orderState, deliveryRegionCode).stream()
                .map(this::toBrief)
                .collect(Collectors.toList());
    }

    private TransportOrderBriefResult toBrief(Order order) {
        return TransportOrderBriefResult.builder()
                .orderId(order.getId())
                .orderNo(order.getOrderNo())
                .orderState(order.getOrderState() == null ? null : order.getOrderState().getValue())
                .vin(order.getDeliveryVin())
                .deliveryStoreCode(order.getDeliveryStoreCode())
                .deliveryRegionCode(order.getDeliveryRegionCode())
                .transportApplyTime(businessTimeZone.fromDate(order.getTransportApplyTime()))
                .transportApplyPersonName(order.getTransportApplyPersonName())
                .build();
    }
}
