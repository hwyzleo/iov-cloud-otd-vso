package net.hwyz.iov.cloud.otd.vso.service.domain.repository;

import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderTimeline;

import java.util.List;

/**
 * 订单时间线仓储接口
 *
 * @author VSO Team
 */
public interface OrderTimelineRepository {

    /**
     * 追加时间线事件
     */
    void append(OrderTimeline timeline);

    /**
     * 按事件时间正序查询订单时间线
     */
    List<OrderTimeline> findByOrderId(String orderId);

    /**
     * 判断指定订单指定事件类型是否已存在（用于重复成功事件去重）
     */
    boolean existsByOrderIdAndEventType(String orderId, String eventType);

    /**
     * 批量查询多个订单的最新事件时间（订单状态时间派生用，避免 N+1）
     *
     * @return 订单业务 ID -> 最新事件时间
     */
    java.util.Map<String, java.time.LocalDateTime> findLatestEventTimeByOrderIds(java.util.List<String> orderIds);
}
