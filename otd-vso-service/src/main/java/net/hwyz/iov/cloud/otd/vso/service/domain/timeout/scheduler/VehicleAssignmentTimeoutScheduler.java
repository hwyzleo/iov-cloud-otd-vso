package net.hwyz.iov.cloud.otd.vso.service.domain.timeout.scheduler;

import cn.hutool.core.util.IdUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.otd.vso.service.domain.gateway.VehicleInventoryGateway;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.VehicleAssignment;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.VehicleAssignmentRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.service.OrderDomainService;
import net.hwyz.iov.cloud.otd.vso.service.domain.service.OrderLockService;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderTimelinePo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.VehicleAssignmentPo;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.AuditRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class VehicleAssignmentTimeoutScheduler {

    private final VehicleAssignmentRepository vehicleAssignmentRepository;
    private final OrderDomainService orderDomainService;
    private final OrderLockService orderLockService;
    private final VehicleInventoryGateway vehicleInventoryGateway;
    private final AuditRepository auditRepository;

    private static final String LOCK_SCENE = "vehicleExpire";

    @Scheduled(fixedRate = 60000)
    public void checkExpiredVehicleAssignments() {
        log.info("开始检查过期的VIN占用");

        List<VehicleAssignmentPo> expiredAssignments = vehicleAssignmentRepository.findExpiredAssignments();

        for (VehicleAssignmentPo assignmentPo : expiredAssignments) {
            try {
                handleExpiredAssignment(assignmentPo);
            } catch (Exception e) {
                log.error("处理过期VIN占用失败：vehicleAssignmentId={}", assignmentPo.getVehicleAssignmentId(), e);
            }
        }

        log.info("完成检查过期的VIN占用，共处理 {} 条记录", expiredAssignments.size());
    }

    private void handleExpiredAssignment(VehicleAssignmentPo assignmentPo) {
        String orderId = assignmentPo.getOrderId();
        String vin = assignmentPo.getVin();

        log.info("处理过期VIN占用：orderId={}, vin={}", orderId, vin);

        orderLockService.executeWithLock(orderId, "SYSTEM", LOCK_SCENE, () -> {
            Optional<VehicleAssignment> assignmentOpt = vehicleAssignmentRepository.findDomainByOrderId(orderId);
            if (assignmentOpt.isEmpty()) {
                return;
            }

            VehicleAssignment assignment = assignmentOpt.get();
            if (!assignment.isExpired()) {
                return;
            }

            assignment.expire();
            vehicleAssignmentRepository.saveDomain(assignment);

            orderDomainService.unassignVehicle(orderId);

            vehicleInventoryGateway.releaseVehicleStatus(vin);

            saveOrderTimeline(orderId, "VEHICLE_ASSIGNMENT_EXPIRED", "VIN占用超时释放",
                    String.format("VIN: %s, 过期时间: %s", vin, assignment.getOccupyExpireTime()));

            log.info("VIN占用超时释放成功：orderId={}, vin={}", orderId, vin);
        });
    }

    private void saveOrderTimeline(String orderId, String eventType, String eventName, String eventRemark) {
        OrderTimelinePo timelinePo = new OrderTimelinePo();
        timelinePo.setTimelineId(IdUtil.fastSimpleUUID());
        timelinePo.setOrderId(orderId);
        timelinePo.setEventType(eventType);
        timelinePo.setEventName(eventName);
        timelinePo.setEventRemark(eventRemark);
        timelinePo.setEventTime(LocalDateTime.now());
        auditRepository.saveTimeline(timelinePo);
    }
}