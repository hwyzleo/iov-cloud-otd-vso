package net.hwyz.iov.cloud.otd.vso.service.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 订单时间线事件领域对象
 *
 * @author VSO Team
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderTimeline {

    private String timelineId;

    private String orderId;

    private String eventType;

    private String eventName;

    private String beforeStatus;

    private String afterStatus;

    private String operatorId;

    private String operatorRole;

    private String operateSource;

    private String relatedDocNo;

    private String result;

    private String failReason;

    private String eventRemark;

    private LocalDateTime eventTime;

    /**
     * 由事件目录构建时间线记录
     */
    public static OrderTimeline of(String timelineId, String orderId, OrderTimelineEventType type,
                                   String beforeStatus, String afterStatus, String operatorId,
                                   String operatorRole, String operateSource, String relatedDocNo,
                                   String failReason, String eventRemark) {
        return OrderTimeline.builder()
                .timelineId(timelineId)
                .orderId(orderId)
                .eventType(type.getCode())
                .eventName(type.getName())
                .beforeStatus(beforeStatus)
                .afterStatus(afterStatus)
                .operatorId(operatorId)
                .operatorRole(operatorRole)
                .operateSource(operateSource)
                .relatedDocNo(relatedDocNo)
                .result(failReason == null ? "success" : "failed")
                .failReason(failReason)
                .eventRemark(eventRemark)
                .eventTime(LocalDateTime.now())
                .build();
    }
}
