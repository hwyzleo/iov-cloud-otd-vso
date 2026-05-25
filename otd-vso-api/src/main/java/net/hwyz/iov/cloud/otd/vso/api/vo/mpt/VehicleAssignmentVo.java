package net.hwyz.iov.cloud.otd.vso.api.vo.mpt;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 配车信息响应
 */
@Data
public class VehicleAssignmentVo {

    private String vehicleAssignmentId;
    private String orderId;
    private String orderNo;
    private String vin;
    private String vehicleId;
    private String assignStatus;
    private String assignStatusName;
    private String assignmentType;
    private String assignmentTypeName;
    private LocalDateTime occupyExpireTime;
    private LocalDateTime assignTime;
    private LocalDateTime bindTime;
    private LocalDateTime releaseTime;
    private String unbindReason;
    private Integer manualAssignFlag;
    private String manualAssignReason;
}