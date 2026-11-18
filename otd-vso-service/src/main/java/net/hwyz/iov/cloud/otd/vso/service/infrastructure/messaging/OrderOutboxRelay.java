package net.hwyz.iov.cloud.otd.vso.service.infrastructure.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderOutboxRecord;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderOutboxRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.config.VsoOutboxProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 订单出站消息中继（OTD-VSO-DSN-CR-012）
 * <p>
 * 按有界批次扫描 PENDING／FAILED_RETRYABLE 记录，使用不可变载荷与注册 Topic／Key 发布，
 * 仅在 Broker 确认后标记 PUBLISHED；超过重试上限置 DEAD 并告警。
 * 中继不参与业务事务：发布失败不回滚已提交的订单状态。
 *
 * @author VSO Team
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "vso.outbox.relay.enabled", havingValue = "true", matchIfMissing = true)
public class OrderOutboxRelay {

    private static final int MAX_ERROR_LENGTH = 2000;

    private final OrderOutboxRepository orderOutboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final VsoOutboxProperties outboxProperties;

    /**
     * 定时扫描并发布待发送记录
     */
    @Scheduled(fixedDelayString = "${vso.outbox.relay.scan-interval-ms:10000}")
    public void relayMessages() {
        List<OrderOutboxRecord> records = orderOutboxRepository.listPublishable(new Date(),
                outboxProperties.getRelay().getBatchSize());
        if (records.isEmpty()) {
            return;
        }
        log.info("VSO 出站中继扫描到 {} 条待发布记录", records.size());
        for (OrderOutboxRecord record : records) {
            relayOne(record);
        }
    }

    private void relayOne(OrderOutboxRecord record) {
        try {
            kafkaTemplate.send(record.getTopic(), record.getMessageKey(), record.getPayload())
                    .get(30, TimeUnit.SECONDS);
            orderOutboxRepository.markPublished(record.getId());
            log.info("出站事件发布成功: eventId={}, topic={}, key={}", record.getEventId(), record.getTopic(),
                    record.getMessageKey());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            handleFailure(record, e);
        } catch (Exception e) {
            handleFailure(record, e);
        }
    }

    private void handleFailure(OrderOutboxRecord record, Exception e) {
        int retryCount = record.getRetryCount() == null ? 1 : record.getRetryCount() + 1;
        String error = truncate(e.getMessage());
        VsoOutboxProperties.Relay relay = outboxProperties.getRelay();
        if (retryCount >= relay.getMaxRetry()) {
            orderOutboxRepository.markDead(record.getId(), error);
            log.error("出站事件置 DEAD，需人工处置: eventId={}, retryCount={}, error={}",
                    record.getEventId(), retryCount, error);
            return;
        }
        Date nextRetryTime = new Date(System.currentTimeMillis() + backoffMillis(relay, retryCount));
        orderOutboxRepository.markRetryable(record.getId(), retryCount, nextRetryTime, error);
        log.warn("出站事件发布失败，安排重试: eventId={}, retryCount={}, nextRetryTime={}, error={}",
                record.getEventId(), retryCount, nextRetryTime, error);
    }

    /**
     * 指数退避：base × 2^(retryCount-1)，封顶 maxBackoffMs
     */
    private long backoffMillis(VsoOutboxProperties.Relay relay, int retryCount) {
        long backoff = relay.getBaseBackoffMs() << Math.min(retryCount - 1, 16);
        return Math.min(backoff, relay.getMaxBackoffMs());
    }

    private String truncate(String error) {
        if (error == null) {
            return null;
        }
        return error.length() <= MAX_ERROR_LENGTH ? error : error.substring(0, MAX_ERROR_LENGTH - 3) + "...";
    }
}
