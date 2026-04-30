package net.hwyz.iov.cloud.otd.vso.service.domain.model;

/**
 * 订单领域事件接口
 *
 * @author VSO Team
 */
public interface OrderDomainEvent {

    /**
     * 事件类型
     */
    String getEventType();

    /**
     * 事件描述
     */
    String getDescription();

    /**
     * 事件发生时间
     */
    java.time.LocalDateTime getOccurTime();

}
