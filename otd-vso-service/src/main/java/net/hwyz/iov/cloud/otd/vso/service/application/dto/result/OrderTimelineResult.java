package net.hwyz.iov.cloud.otd.vso.service.application.dto.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 订单时间线事件结果
 *
 * @author VSO Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderTimelineResult {

    private String eventType;
    private String eventName;
    private LocalDateTime eventTime;
    private String beforeStatus;
    private String afterStatus;
    /**
     * 操作人（MPT 运营核对）
     */
    private String operatorId;
    private String operatorRole;
    private String operateSource;
    private String relatedDocNo;
    private String result;
    private String failReason;
    private String eventRemark;
}
