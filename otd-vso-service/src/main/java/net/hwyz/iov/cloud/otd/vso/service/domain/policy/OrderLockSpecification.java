package net.hwyz.iov.cloud.otd.vso.service.domain.policy;

import net.hwyz.iov.cloud.otd.vso.service.domain.model.Order;
import net.hwyz.iov.cloud.otd.vso.service.domain.service.OrderStateMachine;
import org.springframework.stereotype.Component;

/**
 * 锁单前校验规约
 *
 * @author VSO Team
 */
@Component
public class OrderLockSpecification implements Specification<Order> {

    @Override
    public boolean isSatisfiedBy(Order order) {
        return validateStatus(order)
            && validateCustomerInfo(order)
            && validateVehicleInfo(order)
            && validateAmount(order)
            && validateContract(order)
            && validateDeposit(order);
    }

    /**
     * 验证订单状态
     */
    private boolean validateStatus(Order order) {
        return "PENDING_LOCK".equals(order.getMainStatus());
    }

    /**
     * 验证客户信息已确认
     */
    private boolean validateCustomerInfo(Order order) {
        return order.getCustomerInfo() != null && order.getCustomerInfo().isComplete();
    }

    /**
     * 验证车辆信息已确认
     */
    private boolean validateVehicleInfo(Order order) {
        return order.getVehicleInfo() != null && order.getVehicleInfo().isComplete();
    }

    /**
     * 验证订单金额已确认
     */
    private boolean validateAmount(Order order) {
        return order.getOrderAmount() != null 
            && order.getOrderAmount().getDealPriceTotal() != null
            && order.getOrderAmount().getDealPriceTotal().getAmount().compareTo(java.math.BigDecimal.ZERO) > 0;
    }

    /**
     * 验证合同已生成并签署
     */
    private boolean validateContract(Order order) {
        // TODO: 检查合同状态是否为已签署
        return true;
    }

    /**
     * 验证定金已支付
     */
    private boolean validateDeposit(Order order) {
        if (order.getOrderAmount() == null) {
            return false;
        }
        // 定金金额必须大于 0
        return order.getOrderAmount().getDepositAmount() != null
            && order.getOrderAmount().getDepositAmount().getAmount().compareTo(java.math.BigDecimal.ZERO) > 0;
    }

}
