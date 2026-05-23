package net.hwyz.iov.cloud.otd.vso.service.domain.service;

import lombok.RequiredArgsConstructor;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Order;
import net.hwyz.iov.cloud.otd.vso.service.domain.policy.DuplicateOrderSpecification;
import net.hwyz.iov.cloud.otd.vso.service.domain.policy.OrderLockSpecification;
import net.hwyz.iov.cloud.otd.vso.service.domain.policy.OrderSubmitSpecification;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.OrderMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.VehicleAssignmentMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.VehicleAssignmentPo;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单校验服务
 *
 * @author VSO Team
 */
@Service
@RequiredArgsConstructor
public class OrderValidationService {

    private final OrderSubmitSpecification submitSpecification;
    private final OrderLockSpecification lockSpecification;
    private final DuplicateOrderSpecification duplicateSpecification;
    private final VehicleAssignmentMapper vehicleAssignmentMapper;
    private final OrderMapper orderMapper;

    /**
     * 校验订单是否可以提交
     *
     * @param order 订单
     * @throws IllegalArgumentException 校验失败时抛出
     */
    public void validateForSubmit(Order order) {
        // 校验重复订单
        boolean notDuplicate = duplicateSpecification
            .withOrderType(order.getOrderType())
            .withModelCode(order.getVehicleInfo() != null ? order.getVehicleInfo().getModelCode() : null)
            .isSatisfiedBy(order);
        
        if (!notDuplicate) {
            throw new IllegalArgumentException("存在重复订单，无法提交");
        }

        // 校验提交条件
        if (!submitSpecification.isSatisfiedBy(order)) {
            throw new IllegalArgumentException("订单不满足提交条件，请检查客户信息、车辆信息、归属信息、金额等是否完整");
        }
    }

    /**
     * 校验订单是否可以锁单
     *
     * @param order 订单
     * @throws IllegalArgumentException 校验失败时抛出
     */
    public void validateForLock(Order order) {
        if (!lockSpecification.isSatisfiedBy(order)) {
            throw new IllegalArgumentException("订单不满足锁单条件，请检查客户信息、车辆信息、金额、合同、定金等是否完整");
        }
    }

    /**
     * 校验客户信息完整性
     *
     * @param order 订单
     * @throws IllegalArgumentException 校验失败时抛出
     */
    public void validateCustomerInfo(Order order) {
        if (order.getCustomerInfo() == null || !order.getCustomerInfo().isComplete()) {
            throw new IllegalArgumentException("客户信息不完整");
        }
    }

    /**
     * 校验车辆信息完整性
     *
     * @param order 订单
     * @throws IllegalArgumentException 校验失败时抛出
     */
    public void validateVehicleInfo(Order order) {
        if (order.getVehicleInfo() == null || !order.getVehicleInfo().isComplete()) {
            throw new IllegalArgumentException("车辆信息不完整");
        }
    }

    /**
     * 校验组织归属完整性
     *
     * @param order 订单
     * @throws IllegalArgumentException 校验失败时抛出
     */
    public void validateOrganizationInfo(Order order) {
        if (order.getOrganizationInfo() == null || !order.getOrganizationInfo().isComplete()) {
            throw new IllegalArgumentException("组织归属信息不完整");
        }
    }

    /**
     * 校验订单金额
     *
     * @param order 订单
     * @throws IllegalArgumentException 校验失败时抛出
     */
    public void validateAmount(Order order) {
        if (order.getOrderAmount() == null) {
            throw new IllegalArgumentException("订单金额未设置");
        }
        if (order.getOrderAmount().getDealPriceTotal() == null 
            || order.getOrderAmount().getDealPriceTotal().getAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("成交总价必须大于 0");
        }
    }

    /**
     * 校验 VIN 是否已被其他订单占用
     *
     * @param vin 车辆识别码
     * @param currentOrderNo 当前订单号（排除自身）
     * @throws IllegalArgumentException 校验失败时抛出
     */
    public void validateVinAvailable(String vin, String currentOrderNo) {
        Map<String, Object> params = new HashMap<>();
        params.put("vin", vin);
        params.put("rowValid", 1);
        List<VehicleAssignmentPo> assignments = vehicleAssignmentMapper.selectPoByMap(params);
        if (assignments.isEmpty()) {
            return;
        }
        OrderPo currentOrderPo = orderMapper.selectByOrderNo(currentOrderNo);
        if (currentOrderPo == null) {
            return;
        }
        for (VehicleAssignmentPo assignment : assignments) {
            if (!assignment.getOrderId().equals(currentOrderPo.getOrderId())) {
                throw new IllegalArgumentException("VIN [" + vin + "] 已被其他订单占用");
            }
        }
    }

}
