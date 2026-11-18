package net.hwyz.iov.cloud.otd.vso.service.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.hwyz.iov.cloud.otd.vso.service.common.time.BusinessTimeZone;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Order;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderOutboxRecord;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderState;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderOutboxRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.config.VsoOutboxProperties;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.messaging.JacksonEventPayloadSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 发运履约应用服务测试（OTD-VSO-DSN-CR-012）
 * <p>
 * 覆盖：事件载荷契约（schemaVersion=1、ISO-8601 时间、无 PII）、幂等跳过、待发运查询映射。
 *
 * @author VSO Team
 */
@DisplayName("发运履约 - 发件箱与补偿查询")
class TransportFulfillmentAppServiceTest {

    private static final String ISO_PATTERN =
            "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}[+-]\\d{2}:\\d{2}";
    private static final ZoneId SHANGHAI = ZoneId.of("Asia/Shanghai");

    private final OrderOutboxRepository outboxRepository = mock(OrderOutboxRepository.class);
    private final OrderRepository orderRepository = mock(OrderRepository.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    private TransportFulfillmentAppService service;

    @BeforeEach
    void setUp() {
        VsoOutboxProperties properties = new VsoOutboxProperties();
        service = new TransportFulfillmentAppService(outboxRepository, orderRepository,
                new JacksonEventPayloadSerializer(), properties, BusinessTimeZone.of(SHANGHAI));
    }

    private Order order() {
        return Order.builder()
                .id("ORD-1")
                .orderNo("SO202611180001")
                .orderState(OrderState.APPLY_TRANSPORT)
                .deliveryVin("VIN0000000000001")
                .brandCode("HW")
                .saleModel("M01")
                .configurationCode("CFG-1")
                .deliveryStoreCode("D0001")
                .deliveryRegionCode("R01")
                .licenseCity("310000")
                .transportApplyPersonId("EMP001")
                .transportApplyPersonName("张三")
                .transportApplyTime(new Date(1763441727412L))
                .currentVersionNo(12)
                .build();
    }

    @Test
    @DisplayName("发运申请事件入箱：Topic/Key/载荷契约正确且不含 PII")
    void shouldAppendTransportAppliedEvent() throws Exception {
        when(outboxRepository.existsByEventTypeAndAggregateId(anyString(), anyString())).thenReturn(false);

        service.appendTransportApplied(order());

        ArgumentCaptor<OrderOutboxRecord> captor = ArgumentCaptor.forClass(OrderOutboxRecord.class);
        verify(outboxRepository).insert(captor.capture());
        OrderOutboxRecord record = captor.getValue();

        assertThat(record.getEventType()).isEqualTo("vso.transport.applied");
        assertThat(record.getTopic()).isEqualTo("vso.transport.applied");
        assertThat(record.getMessageKey()).isEqualTo("SO202611180001");
        assertThat(record.getAggregateId()).isEqualTo("ORD-1");
        assertThat(record.getAggregateType()).isEqualTo("ORDER");
        assertThat(record.getAggregateVersion()).isEqualTo(12L);
        assertThat(record.getPublishState()).isEqualTo("PENDING");
        assertThat(record.getRetryCount()).isZero();
        assertThat(record.getEventId()).isNotBlank();

        JsonNode json = objectMapper.readTree(record.getPayload());
        assertThat(json.get("eventType").asText()).isEqualTo("vso.transport.applied");
        assertThat(json.get("schemaVersion").asInt()).isEqualTo(1);
        assertThat(json.get("producer").asText()).isEqualTo("otd-vso");
        assertThat(json.get("occurredAt").asText()).matches(ISO_PATTERN);
        assertThat(json.get("aggregateVersion").asLong()).isEqualTo(12L);

        JsonNode payload = json.path("payload");
        assertThat(payload.get("orderNo").asText()).isEqualTo("SO202611180001");
        assertThat(payload.get("orderId").asText()).isEqualTo("ORD-1");
        assertThat(payload.get("orderState").asInt()).isEqualTo(470);
        assertThat(payload.get("vin").asText()).isEqualTo("VIN0000000000001");
        assertThat(payload.get("deliveryStoreCode").asText()).isEqualTo("D0001");
        assertThat(payload.get("deliveryRegionCode").asText()).isEqualTo("R01");
        assertThat(payload.get("transportApplyTime").asText()).matches(ISO_PATTERN);
        assertThat(payload.has("orderPersonPhone")).isFalse();
        assertThat(payload.has("orderPersonIdNum")).isFalse();
    }

    @Test
    @DisplayName("重复触发幂等：已存在生效事件时不再入箱")
    void shouldSkipDuplicateEvent() {
        when(outboxRepository.existsByEventTypeAndAggregateId("vso.transport.applied", "ORD-1")).thenReturn(true);

        service.appendTransportApplied(order());

        verify(outboxRepository, never()).insert(any());
    }

    @Test
    @DisplayName("待发运查询：映射履约字段并透传区域条件")
    void shouldSearchTransportApplied() {
        Order order = order();
        when(orderRepository.findTransportApplied(OrderState.APPLY_TRANSPORT, "R01")).thenReturn(List.of(order));

        var results = service.searchTransportApplied(OrderState.APPLY_TRANSPORT.getValue(), "R01");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getOrderNo()).isEqualTo("SO202611180001");
        assertThat(results.get(0).getOrderState()).isEqualTo(470);
        assertThat(results.get(0).getVin()).isEqualTo("VIN0000000000001");
        assertThat(results.get(0).getTransportApplyTime()).isNotNull()
                .isEqualTo(LocalDateTime.ofInstant(Instant.ofEpochMilli(1763441727412L), SHANGHAI));
        verify(orderRepository, times(1)).findTransportApplied(OrderState.APPLY_TRANSPORT, "R01");
    }

    @Test
    @DisplayName("待发运查询：非法状态直接拒绝")
    void shouldRejectIllegalState() {
        assertThatThrownBy(() -> service.searchTransportApplied(123456, null))
                .isInstanceOf(IllegalArgumentException.class);
        verify(orderRepository, never()).findTransportApplied(any(), any());
    }

    @Test
    @DisplayName("空订单不写入发件箱")
    void shouldIgnoreBlankOrder() {
        service.appendTransportApplied(null);
        service.appendTransportApplied(Order.builder().build());

        verify(outboxRepository, never()).insert(any());
    }
}
