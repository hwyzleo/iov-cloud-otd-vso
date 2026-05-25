package net.hwyz.iov.cloud.otd.vso.service.domain.model;

import cn.hutool.core.util.IdUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.otd.vso.api.enums.AssignStatus;
import net.hwyz.iov.cloud.otd.vso.api.enums.AssignmentType;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.OrderStateNotAllowedException;

import java.time.LocalDateTime;

/**
 * 配车领域模型
 */
@Slf4j
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleAssignment {

    private String vehicleAssignmentId;
    private String orderId;
    private AssignmentType assignmentType;
    private String vehicleSourceType;
    private String vin;
    private String vehicleId;
    private AssignStatus assignStatus;
    private Integer manualAssignFlag;
    private String manualAssignReason;
    private String unbindReason;
    private LocalDateTime occupyExpireTime;
    private LocalDateTime assignTime;
    private LocalDateTime bindTime;
    private LocalDateTime releaseTime;

    /**
     * 分配VIN
     */
    public static VehicleAssignment assign(String orderId, String vin, int occupancyHours) {
        LocalDateTime now = LocalDateTime.now();
        return VehicleAssignment.builder()
                .vehicleAssignmentId(IdUtil.fastSimpleUUID())
                .orderId(orderId)
                .assignmentType(AssignmentType.ASSIGN)
                .vin(vin)
                .assignStatus(AssignStatus.ASSIGNED)
                .manualAssignFlag(1)
                .assignTime(now)
                .occupyExpireTime(now.plusHours(occupancyHours))
                .build();
    }

    /**
     * 换绑VIN
     */
    public void reassign(String newVin, int occupancyHours) {
        if (this.assignStatus != AssignStatus.ASSIGNED && this.assignStatus != AssignStatus.BOUND) {
            throw new OrderStateNotAllowedException(this.orderId, null, "REASSIGN_VEHICLE");
        }
        this.assignmentType = AssignmentType.REASSIGN;
        this.vin = newVin;
        this.assignStatus = AssignStatus.ASSIGNED;
        this.bindTime = null;
        LocalDateTime now = LocalDateTime.now();
        this.assignTime = now;
        this.occupyExpireTime = now.plusHours(occupancyHours);
    }

    /**
     * 解绑VIN
     */
    public void unbind(String reason) {
        if (this.assignStatus != AssignStatus.ASSIGNED && this.assignStatus != AssignStatus.BOUND) {
            throw new OrderStateNotAllowedException(this.orderId, null, "UNBIND_VEHICLE");
        }
        this.assignmentType = AssignmentType.UNBIND;
        this.assignStatus = AssignStatus.UNBOUND;
        this.unbindReason = reason;
        this.releaseTime = LocalDateTime.now();
    }

    /**
     * 释放VIN（订单取消/退款）
     */
    public void release(String reason) {
        if (this.assignStatus == AssignStatus.RELEASED || this.assignStatus == AssignStatus.EXPIRED 
            || this.assignStatus == AssignStatus.UNBOUND) {
            return;
        }
        this.assignmentType = AssignmentType.RELEASE;
        this.assignStatus = AssignStatus.RELEASED;
        this.unbindReason = reason;
        this.releaseTime = LocalDateTime.now();
    }

    /**
     * 过期释放VIN
     */
    public void expire() {
        if (this.assignStatus != AssignStatus.ASSIGNED) {
            return;
        }
        this.assignmentType = AssignmentType.EXPIRE;
        this.assignStatus = AssignStatus.EXPIRED;
        this.releaseTime = LocalDateTime.now();
    }

    /**
     * 绑定VIN（进入发运流程）
     */
    public void bind() {
        if (this.assignStatus == AssignStatus.ASSIGNED) {
            this.assignmentType = AssignmentType.BIND;
            this.assignStatus = AssignStatus.BOUND;
            this.bindTime = LocalDateTime.now();
        }
    }

    /**
     * 检查是否已过期
     */
    public boolean isExpired() {
        return this.assignStatus == AssignStatus.ASSIGNED 
            && this.occupyExpireTime != null 
            && LocalDateTime.now().isAfter(this.occupyExpireTime);
    }

    /**
     * 检查VIN是否被占用
     */
    public boolean isOccupied() {
        return this.assignStatus == AssignStatus.ASSIGNED || this.assignStatus == AssignStatus.BOUND;
    }
}