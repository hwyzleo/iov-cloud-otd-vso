package net.hwyz.iov.cloud.otd.vso.service.domain.policy;

import lombok.RequiredArgsConstructor;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Order;
import org.springframework.stereotype.Component;

/**
 * 订单提交前校验规约
 *
 * @author VSO Team
 */
@Component
@RequiredArgsConstructor
public class OrderSubmitSpecification implements Specification<Order> {

    @Override
    public boolean isSatisfiedBy(Order order) {
        return validateCustomerInfo(order)
            && validateVehicleInfo(order)
            && validateOrganizationInfo(order)
            && validateAmount(order)
            && validateStatus(order);
    }

    /**
     * 验证客户信息
     */
    private boolean validateCustomerInfo(Order order) {
        if (order.getCustomerInfo() == null) {
            return false;
        }
        return order.getCustomerInfo().isComplete();
    }

    /**
     * 验证车辆信息
     */
    private boolean validateVehicleInfo(Order order) {
        if (order.getVehicleInfo() == null) {
            return false;
        }
        return order.getVehicleInfo().isComplete();
    }

    /**
     * 验证组织归属
     */
    private boolean validateOrganizationInfo(Order order) {
        if (order.getOrganizationInfo() == null) {
            return false;
        }
        return order.getOrganizationInfo().isComplete();
    }

    /**
     * 验证订单金额
     */
    private boolean validateAmount(Order order) {
        if (order.getOrderAmount() == null) {
            return false;
        }
        // 成交总价必须大于 0
        return order.getOrderAmount().getDealPriceTotal() != null
            && order.getOrderAmount().getDealPriceTotal().getAmount().compareTo(java.math.BigDecimal.ZERO) > 0;
    }

    /**
     * 验证订单状态
     */
    private boolean validateStatus(Order order) {
        String status = order.getMainStatus();
        // 只有待创建、待提交状态的订单才能提交
        return "PENDING_CREATE".equals(status) || "PENDING_SUBMIT".equals(status);
    }

}
