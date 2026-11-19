package net.hwyz.iov.cloud.otd.vso.service.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.hwyz.iov.cloud.otd.vso.service.common.time.BusinessTimeZone;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.DeliveryRecord;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Order;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderOutboxRecord;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderState;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderOutboxRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.config.VsoOutboxProperties;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.messaging.JacksonEventPayloadSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 交付履约应用服务测试（OTD-VSO-DSN-CR-016）
 * <p>
 * 覆盖：事件字段契约（schemaVersion=1、ISO-8601、无 PII）、Topic/Key、aggregateVersion、
 * 幂等跳过、缺失必要标识拒绝入箱而非发布伪事实。
 *
 * @author VSO Team
 */
@DisplayName("交付履约 - 交付事件入箱")
class DeliveryFulfillmentAppServiceTest {

    private static final String ISO_PATTERN =
            "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}[+-]\\d{2}:\\d{2}";
    private static final ZoneId SHANGHAI = ZoneId.of("Asia/Shanghai");

    private final OrderOutboxRepository outboxRepository = mock(OrderOutboxRepository.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    private DeliveryFulfillmentAppService service;

    @BeforeEach
    void setUp() {
        VsoOutboxProperties properties = new VsoOutboxProperties();
        service = new DeliveryFulfillmentAppService(outboxRepository,
                new JacksonEventPayloadSerializer(), properties, BusinessTimeZone.of(SHANGHAI));
    }

    @Test
    @DisplayName("交付事件入箱：Topic/Key/载荷契约正确且不含 PII")
    void shouldAppendDeliveredEvent() throws Exception {
        when(outboxRepository.existsByEventTypeAndAggregateId("vso.order.delivered", "ORD-1")).thenReturn(false);

        service.appendDelivered(order(), record());

        ArgumentCaptor<OrderOutboxRecord> captor = ArgumentCaptor.forClass(OrderOutboxRecord.class);
        verify(outboxRepository).insert(captor.capture());
        OrderOutboxRecord record = captor.getValue();

        assertThat(record.getEventType()).isEqualTo("vso.order.delivered");
        assertThat(record.getTopic()).isEqualTo("vso.order.delivered");
        assertThat(record.getMessageKey()).isEqualTo("SO202611190001");
        assertThat(record.getAggregateId()).isEqualTo("ORD-1");
        assertThat(record.getAggregateType()).isEqualTo("ORDER");
        assertThat(record.getAggregateVersion()).isEqualTo(12L);
        assertThat(record.getPublishState()).isEqualTo("PENDING");
        assertThat(record.getRetryCount()).isZero();
        assertThat(record.getEventId()).isNotBlank();

        JsonNode json = objectMapper.readTree(record.getPayload());
        assertThat(json.get("eventType").asText()).isEqualTo("vso.order.delivered");
        assertThat(json.get("schemaVersion").asInt()).isEqualTo(1);
        assertThat(json.get("producer").asText()).isEqualTo("otd-vso");
        assertThat(json.get("occurredAt").asText()).matches(ISO_PATTERN);
        assertThat(json.get("aggregateVersion").asLong()).isEqualTo(12L);

        JsonNode payload = json.path("payload");
        assertThat(payload.get("orderId").asText()).isEqualTo("ORD-1");
        assertThat(payload.get("orderNo").asText()).isEqualTo("SO202611190001");
        assertThat(payload.get("orderState").asInt()).isEqualTo(650);
        assertThat(payload.get("vin").asText()).isEqualTo("VIN0000000000001");
        assertThat(payload.get("deliveryRecordId").asText()).isEqualTo("DR-1");
        assertThat(payload.get("deliveryStoreCode").asText()).isEqualTo("D0001");
        assertThat(payload.get("actualDeliveryTime").asText()).matches(ISO_PATTERN);
        assertThat(payload.get("actualDeliveryTime").asText()).isEqualTo("2026-11-19T15:29:58.123+08:00");
        assertThat(payload.has("accountId")).isFalse();
        assertThat(payload.has("orderPersonName")).isFalse();
        assertThat(payload.has("deliveryPersonId")).isFalse();
        assertThat(payload.has("deliveryRemark")).isFalse();
    }

    @Test
    @DisplayName("重复触发幂等：已存在生效事件时不再入箱")
    void shouldSkipDuplicateEvent() {
        when(outboxRepository.existsByEventTypeAndAggregateId("vso.order.delivered", "ORD-1")).thenReturn(true);

        service.appendDelivered(order(), record());

        verify(outboxRepository, never()).insert(any());
    }

    @Test
    @DisplayName("缺失必要标识拒绝入箱，不发布伪事实")
    void shouldRejectWhenRequiredIdentifierMissing() {
        service.appendDelivered(null, record());
        service.appendDelivered(Order.builder().build(), record());
        service.appendDelivered(order(), null);
        service.appendDelivered(order(), DeliveryRecord.builder().orderId("ORD-1").build());

        verify(outboxRepository, never()).insert(any());
    }

    private Order order() {
        return Order.builder()
                .id("ORD-1")
                .orderNo("SO202611190001")
                .orderState(OrderState.DELIVERED)
                .deliveryVin("VIN0000000000001")
                .currentVersionNo(12)
                .build();
    }

    private DeliveryRecord record() {
        return DeliveryRecord.builder()
                .deliveryRecordId("DR-1")
                .orderId("ORD-1")
                .deliveryStatus("delivered")
                .deliveryStoreCode("D0001")
                .vehicleVin("VIN0000000000001")
                .actualDeliveryTime(LocalDateTime.of(2026, 11, 19, 15, 29, 58, 123_000_000))
                .build();
    }
}
