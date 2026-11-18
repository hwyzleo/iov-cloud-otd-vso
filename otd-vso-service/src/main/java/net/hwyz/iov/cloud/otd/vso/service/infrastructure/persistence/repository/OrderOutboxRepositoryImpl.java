package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderOutboxRecord;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderOutboxRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.VsoOutboxMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.VsoOutboxPo;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单出站消息仓储实现
 *
 * @author VSO Team
 */
@Repository
@RequiredArgsConstructor
public class OrderOutboxRepositoryImpl implements OrderOutboxRepository {

    private final VsoOutboxMapper vsoOutboxMapper;

    @Override
    public void insert(OrderOutboxRecord record) {
        vsoOutboxMapper.insertPo(VsoOutboxPo.builder()
                .eventId(record.getEventId())
                .eventType(record.getEventType())
                .aggregateType(record.getAggregateType())
                .aggregateId(record.getAggregateId())
                .aggregateVersion(record.getAggregateVersion())
                .topic(record.getTopic())
                .messageKey(record.getMessageKey())
                .payload(record.getPayload())
                .publishState(record.getPublishState())
                .retryCount(record.getRetryCount() == null ? 0 : record.getRetryCount())
                .build());
    }

    @Override
    public boolean existsByEventTypeAndAggregateId(String eventType, String aggregateId) {
        return vsoOutboxMapper.countByEventTypeAndAggregateId(eventType, aggregateId) > 0;
    }

    @Override
    public List<OrderOutboxRecord> listPublishable(Date now, int limit) {
        return vsoOutboxMapper.selectPublishable(now, limit).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public int markPublished(Long id) {
        return vsoOutboxMapper.markPublished(id);
    }

    @Override
    public int markRetryable(Long id, int retryCount, Date nextRetryTime, String error) {
        return vsoOutboxMapper.markRetryable(id, retryCount, nextRetryTime, error);
    }

    @Override
    public int markDead(Long id, String error) {
        return vsoOutboxMapper.markDead(id, error);
    }

    private OrderOutboxRecord toDomain(VsoOutboxPo po) {
        return OrderOutboxRecord.builder()
                .id(po.getId())
                .eventId(po.getEventId())
                .eventType(po.getEventType())
                .aggregateType(po.getAggregateType())
                .aggregateId(po.getAggregateId())
                .aggregateVersion(po.getAggregateVersion())
                .topic(po.getTopic())
                .messageKey(po.getMessageKey())
                .payload(po.getPayload())
                .publishState(po.getPublishState())
                .retryCount(po.getRetryCount())
                .nextRetryTime(po.getNextRetryTime())
                .publishedAt(po.getPublishedAt())
                .lastError(po.getLastError())
                .build();
    }
}
