package net.hwyz.iov.cloud.otd.vso.service.domain.repository;

import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderOutboxRecord;

import java.util.Date;
import java.util.List;

/**
 * 订单出站消息仓储（领域定义的持久化端口，OTD-VSO-DSN-CR-012）
 *
 * @author VSO Team
 */
public interface OrderOutboxRepository {

    /**
     * 写入待发布事件（与订单状态更新同一本地事务）
     *
     * @param record 发布记录
     */
    void insert(OrderOutboxRecord record);

    /**
     * 判断聚合是否已有指定类型的生效事件（业务幂等）
     *
     * @param eventType   事件类型
     * @param aggregateId 聚合 ID
     * @return 已存在返回 true
     */
    boolean existsByEventTypeAndAggregateId(String eventType, String aggregateId);

    /**
     * 扫描可发布记录
     *
     * @param now   当前时间
     * @param limit 批大小
     * @return 待发布记录
     */
    List<OrderOutboxRecord> listPublishable(Date now, int limit);

    /**
     * 标记已发布
     *
     * @param id 主键
     * @return 影响行数
     */
    int markPublished(Long id);

    /**
     * 标记可重试
     *
     * @param id            主键
     * @param retryCount    已重试次数
     * @param nextRetryTime 下次重试时间
     * @param error         脱敏错误
     * @return 影响行数
     */
    int markRetryable(Long id, int retryCount, Date nextRetryTime, String error);

    /**
     * 标记终态失败
     *
     * @param id    主键
     * @param error 脱敏错误
     * @return 影响行数
     */
    int markDead(Long id, String error);
}
