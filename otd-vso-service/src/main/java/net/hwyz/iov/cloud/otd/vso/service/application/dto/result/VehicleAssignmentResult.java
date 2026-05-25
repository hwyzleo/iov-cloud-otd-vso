package net.hwyz.iov.cloud.otd.vso.service.application.dto.result;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 配车信息结果
 */
@Data
@Builder
public class VehicleAssignmentResult {

    private String vehicleAssignmentId;
    private String orderId;
    private String orderNo;
    private String vin;
    private String vehicleId;
    private String assignStatus;
    private String assignStatusName;
    private LocalDateTime occupyExpireTime;
    private LocalDateTime assignTime;
    private LocalDateTime bindTime;
    private LocalDateTime releaseTime;
    private String unbindReason;

}