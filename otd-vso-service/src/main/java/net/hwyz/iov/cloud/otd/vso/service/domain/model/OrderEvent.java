package net.hwyz.iov.cloud.otd.vso.service.domain.model;

import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 订单领域事件实现
 *
 * @author VSO Team
 */
@Getter
public class OrderEvent implements OrderDomainEvent {

    /**
     * 事件类型
     */
    private final String eventType;

    /**
     * 事件描述
     */
    private final String description;

    /**
     * 事件发生时间
     */
    private final LocalDateTime occurTime;

    public OrderEvent(String eventType, String description) {
        this.eventType = eventType;
        this.description = description;
        this.occurTime = LocalDateTime.now();
    }

}
