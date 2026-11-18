package net.hwyz.iov.cloud.otd.vso.service.infrastructure.messaging;

import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderOutboxRecord;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderOutboxRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.config.VsoOutboxProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 出站消息中继测试（OTD-VSO-DSN-CR-012）
 * <p>
 * 覆盖：Broker 确认后置 PUBLISHED、失败退避重试、超过上限置 DEAD。
 *
 * @author VSO Team
 */
@DisplayName("出站消息中继")
class OrderOutboxRelayTest {

    private final OrderOutboxRepository repository = mock(OrderOutboxRepository.class);
    @SuppressWarnings("unchecked")
    private final KafkaTemplate<String, String> kafkaTemplate = mock(KafkaTemplate.class);
    private final VsoOutboxProperties properties = new VsoOutboxProperties();

    private OrderOutboxRelay relay;

    @BeforeEach
    void setUp() {
        relay = new OrderOutboxRelay(repository, kafkaTemplate, properties);
    }

    private OrderOutboxRecord record(Integer retryCount) {
        return OrderOutboxRecord.builder()
                .id(1L)
                .eventId("EVT-1")
                .eventType("vso.transport.applied")
                .aggregateType("ORDER")
                .aggregateId("ORD-1")
                .aggregateVersion(1L)
                .topic("vso.transport.applied")
                .messageKey("SO1")
                .payload("{\"eventId\":\"EVT-1\"}")
                .publishState("PENDING")
                .retryCount(retryCount)
                .build();
    }

    @SuppressWarnings("unchecked")
    private CompletableFuture<SendResult<String, String>> success() {
        CompletableFuture<SendResult<String, String>> future = new CompletableFuture<>();
        future.complete(mock(SendResult.class));
        return future;
    }

    @Test
    @DisplayName("Broker 确认后置 PUBLISHED")
    void shouldMarkPublishedOnSuccess() {
        when(repository.listPublishable(any(Date.class), anyInt())).thenReturn(List.of(record(0)));
        when(kafkaTemplate.send(anyString(), anyString(), anyString())).thenReturn(success());

        relay.relayMessages();

        verify(kafkaTemplate).send("vso.transport.applied", "SO1", "{\"eventId\":\"EVT-1\"}");
        verify(repository).markPublished(1L);
        verify(repository, never()).markRetryable(anyLong(), anyInt(), any(), anyString());
        verify(repository, never()).markDead(anyLong(), anyString());
    }

    @Test
    @DisplayName("发布失败安排退避重试")
    void shouldScheduleRetryOnFailure() {
        when(repository.listPublishable(any(Date.class), anyInt())).thenReturn(List.of(record(0)));
        CompletableFuture<SendResult<String, String>> failed = new CompletableFuture<>();
        failed.completeExceptionally(new IllegalStateException("kafka down"));
        when(kafkaTemplate.send(anyString(), anyString(), anyString())).thenReturn(failed);

        long before = System.currentTimeMillis();
        relay.relayMessages();

        ArgumentCaptor<Date> nextRetry = ArgumentCaptor.forClass(Date.class);
        verify(repository).markRetryable(eq(1L), eq(1), nextRetry.capture(), anyString());
        assertThat(nextRetry.getValue().getTime()).isGreaterThanOrEqualTo(before + 5000L);
        verify(repository, never()).markPublished(anyLong());
        verify(repository, never()).markDead(anyLong(), anyString());
    }

    @Test
    @DisplayName("超过重试上限置 DEAD")
    void shouldMarkDeadWhenRetryExhausted() {
        when(repository.listPublishable(any(Date.class), anyInt()))
                .thenReturn(List.of(record(properties.getRelay().getMaxRetry() - 1)));
        CompletableFuture<SendResult<String, String>> failed = new CompletableFuture<>();
        failed.completeExceptionally(new IllegalStateException("kafka down"));
        when(kafkaTemplate.send(anyString(), anyString(), anyString())).thenReturn(failed);

        relay.relayMessages();

        verify(repository).markDead(eq(1L), anyString());
        verify(repository, never()).markRetryable(anyLong(), anyInt(), any(), anyString());
    }
}
