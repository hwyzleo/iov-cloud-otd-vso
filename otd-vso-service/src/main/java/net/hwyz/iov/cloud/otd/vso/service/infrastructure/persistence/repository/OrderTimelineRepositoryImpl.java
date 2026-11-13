package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderTimeline;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderTimelineRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.converter.DeliveryPoConverter;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.OrderTimelineMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderTimelinePo;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单时间线仓储实现
 *
 * @author VSO Team
 */
@Repository
@RequiredArgsConstructor
public class OrderTimelineRepositoryImpl implements OrderTimelineRepository {

    private final OrderTimelineMapper orderTimelineMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void append(OrderTimeline timeline) {
        orderTimelineMapper.insertPo(DeliveryPoConverter.fromDomain(timeline));
    }

    @Override
    public List<OrderTimeline> findByOrderId(String orderId) {
        return orderTimelineMapper.selectByOrderId(orderId).stream()
                .map(DeliveryPoConverter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public java.util.Map<String, java.time.LocalDateTime> findLatestEventTimeByOrderIds(java.util.List<String> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) {
            return java.util.Map.of();
        }
        java.util.Map<String, java.time.LocalDateTime> result = new java.util.HashMap<>();
        orderTimelineMapper.selectLatestEventTimeByOrderIds(orderIds)
                .forEach(po -> result.putIfAbsent(po.getOrderId(), po.getEventTime()));
        return result;
    }

    @Override
    public boolean existsByOrderIdAndEventType(String orderId, String eventType) {
        return countByOrderIdAndEventType(orderId, eventType) > 0;
    }

    @Override
    public int countByOrderIdAndEventType(String orderId, String eventType) {
        return orderTimelineMapper.countByOrderIdAndEventType(orderId, eventType);
    }
}
