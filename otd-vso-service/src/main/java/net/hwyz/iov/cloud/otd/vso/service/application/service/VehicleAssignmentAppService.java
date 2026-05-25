package net.hwyz.iov.cloud.otd.vso.service.application.service;

import cn.hutool.core.util.IdUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.otd.vso.api.enums.AssignStatus;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.AssignVehicleCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.ReassignVehicleCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.UnbindVehicleCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.VehicleAssignmentResult;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.OrderNotExistException;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.OrderStateNotAllowedException;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.VinConflictException;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.VinInvalidException;
import net.hwyz.iov.cloud.otd.vso.service.domain.gateway.VehicleInventoryGateway;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Order;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderState;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.VehicleAssignment;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.VehicleAssignmentRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.VehicleOccupancyConfigRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.service.OrderLockService;
import net.hwyz.iov.cloud.otd.vso.service.domain.service.OrderDomainService;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.OrderMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.VehicleAssignmentPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderTimelinePo;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.AuditRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 配车应用服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleAssignmentAppService {

    private final OrderLockService orderLockService;
    private final OrderDomainService orderDomainService;
    private final OrderRepository orderRepository;
    private final VehicleAssignmentRepository vehicleAssignmentRepository;
    private final VehicleOccupancyConfigRepository vehicleOccupancyConfigRepository;
    private final VehicleInventoryGateway vehicleInventoryGateway;
    private final OrderMapper orderMapper;
    private final AuditRepository auditRepository;

    private static final String LOCK_SCENE = "bindVehicle";

    @Transactional(rollbackFor = Exception.class)
    public void assignVehicle(AssignVehicleCmd cmd) {
        log.info("配车绑定VIN：orderNo={}, vin={}", cmd.getOrderNo(), cmd.getVin());
        orderLockService.executeWithLock(cmd.getOrderNo(), cmd.getVin(), LOCK_SCENE, () -> {
            Order order = orderDomainService.loadOrder(cmd.getOrderNo());
            validateForAssign(order, cmd.getVin());
            int occupancyHours = vehicleOccupancyConfigRepository.getDefaultOccupancyHours();
            VehicleAssignment assignment = VehicleAssignment.assign(order.getId(), cmd.getVin(), occupancyHours);
            vehicleAssignmentRepository.saveDomain(assignment);
            order.assignVehicle(cmd.getVin());
            orderRepository.save(order);
            vehicleInventoryGateway.updateVehicleStatusToAllocated(cmd.getVin());
            saveOrderTimeline(order.getId(), "VEHICLE_ASSIGNED", "配车成功", String.format("VIN: %s", cmd.getVin()));
            log.info("配车成功：orderNo={}, vin={}", cmd.getOrderNo(), cmd.getVin());
        });
    }

    @Transactional(rollbackFor = Exception.class)
    public void reassignVehicle(ReassignVehicleCmd cmd) {
        log.info("换绑VIN：orderNo={}, newVin={}", cmd.getOrderNo(), cmd.getNewVin());
        orderLockService.executeWithLock(cmd.getOrderNo(), cmd.getOperatorId(), LOCK_SCENE, () -> {
            Order order = orderDomainService.loadOrder(cmd.getOrderNo());
            validateForReassign(order, cmd.getNewVin());
            Optional<VehicleAssignment> existingAssignment = vehicleAssignmentRepository.findDomainByOrderId(order.getId());
            if (existingAssignment.isEmpty()) {
                throw new OrderStateNotAllowedException("车辆销售订单[" + order.getOrderNo() + "]当前状态[" + order.getOrderState() + "]不允许换绑（无车辆）");
            }
            String oldVin = existingAssignment.get().getVin();
            int occupancyHours = vehicleOccupancyConfigRepository.getDefaultOccupancyHours();
            VehicleAssignment assignment = existingAssignment.get();
            assignment.reassign(cmd.getNewVin(), occupancyHours);
            vehicleAssignmentRepository.saveDomain(assignment);
            order.saveDeliveryVehicle(cmd.getNewVin());
            orderRepository.save(order);
            vehicleInventoryGateway.releaseVehicleStatus(oldVin);
            vehicleInventoryGateway.updateVehicleStatusToAllocated(cmd.getNewVin());
            saveOrderTimeline(order.getId(), "VEHICLE_REASSIGNED", "换绑VIN", String.format("旧VIN: %s, 新VIN: %s", oldVin, cmd.getNewVin()));
            log.info("换绑成功：orderNo={}, oldVin={}, newVin={}", cmd.getOrderNo(), oldVin, cmd.getNewVin());
        });
    }

    @Transactional(rollbackFor = Exception.class)
    public void unbindVehicle(UnbindVehicleCmd cmd) {
        log.info("解绑VIN：orderNo={}, reason={}", cmd.getOrderNo(), cmd.getUnbindReason());
        orderLockService.executeWithLock(cmd.getOrderNo(), cmd.getOperatorId(), LOCK_SCENE, () -> {
            Order order = orderDomainService.loadOrder(cmd.getOrderNo());
            validateForUnbind(order);
            Optional<VehicleAssignment> existingAssignment = vehicleAssignmentRepository.findDomainByOrderId(order.getId());
            if (existingAssignment.isEmpty()) {
                throw new OrderStateNotAllowedException("车辆销售订单[" + order.getOrderNo() + "]当前状态[" + order.getOrderState() + "]不允许解绑（无车辆）");
            }
            String vin = existingAssignment.get().getVin();
            VehicleAssignment assignment = existingAssignment.get();
            assignment.unbind(cmd.getUnbindReason());
            vehicleAssignmentRepository.saveDomain(assignment);
            order.unassignVehicle();
            orderRepository.save(order);
            vehicleInventoryGateway.releaseVehicleStatus(vin);
            saveOrderTimeline(order.getId(), "VEHICLE_UNBOUND", "解绑VIN", String.format("VIN: %s, 原因: %s", vin, cmd.getUnbindReason()));
            log.info("解绑成功：orderNo={}, vin={}", cmd.getOrderNo(), vin);
        });
    }

    public VehicleAssignmentResult getVehicleAssignment(String orderNo) {
        OrderPo orderPo = orderMapper.selectByOrderNo(orderNo);
        if (orderPo == null) {
            throw new OrderNotExistException(orderNo);
        }
        Optional<VehicleAssignmentPo> assignmentPo = vehicleAssignmentRepository.findByOrderId(orderPo.getOrderId());
        if (assignmentPo.isEmpty()) {
            return null;
        }
        VehicleAssignmentPo po = assignmentPo.get();
        return VehicleAssignmentResult.builder()
                .vehicleAssignmentId(po.getVehicleAssignmentId())
                .orderId(po.getOrderId())
                .orderNo(orderNo)
                .vin(po.getVin())
                .vehicleId(po.getVehicleId())
                .assignStatus(po.getAssignStatus())
                .assignStatusName(AssignStatus.fromCode(po.getAssignStatus()) != null ? AssignStatus.fromCode(po.getAssignStatus()).getName() : "")
                .occupyExpireTime(po.getOccupyExpireTime())
                .assignTime(po.getAssignTime())
                .bindTime(po.getBindTime())
                .releaseTime(po.getReleaseTime())
                .unbindReason(po.getUnbindReason())
                .build();
    }

    private void validateForAssign(Order order, String vin) {
        if (order.getOrderState() != OrderState.ARRANGE_PRODUCTION) {
            throw new OrderStateNotAllowedException("车辆销售订单[" + order.getOrderNo() + "]当前状态[" + order.getOrderState() + "]不允许分配车辆");
        }
        if (!vehicleInventoryGateway.validateVinAvailable(vin)) {
            throw new VinInvalidException(vin);
        }
        Optional<VehicleAssignmentPo> occupiedAssignment = vehicleAssignmentRepository.findOccupiedByVin(vin);
        if (occupiedAssignment.isPresent() && !occupiedAssignment.get().getOrderId().equals(order.getId())) {
            throw new VinConflictException(vin);
        }
    }

    private void validateForReassign(Order order, String newVin) {
        if (order.getOrderState() != OrderState.ALLOCATION_VEHICLE) {
            throw new OrderStateNotAllowedException("车辆销售订单[" + order.getOrderNo() + "]当前状态[" + order.getOrderState() + "]不允许换绑车辆");
        }
        if (!vehicleInventoryGateway.validateVinAvailable(newVin)) {
            throw new VinInvalidException(newVin);
        }
        Optional<VehicleAssignmentPo> occupiedAssignment = vehicleAssignmentRepository.findOccupiedByVin(newVin);
        if (occupiedAssignment.isPresent() && !occupiedAssignment.get().getOrderId().equals(order.getId())) {
            throw new VinConflictException(newVin);
        }
    }

    private void validateForUnbind(Order order) {
        if (order.getOrderState() != OrderState.ALLOCATION_VEHICLE) {
            throw new OrderStateNotAllowedException("车辆销售订单[" + order.getOrderNo() + "]当前状态[" + order.getOrderState() + "]不允许解绑车辆");
        }
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