package net.hwyz.iov.cloud.otd.vso.service.domain.policy;

import lombok.RequiredArgsConstructor;
import net.hwyz.iov.cloud.otd.vso.api.enums.OrderType;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Order;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 重复订单校验规约
 *
 * @author VSO Team
 */
@Component
@RequiredArgsConstructor
public class DuplicateOrderSpecification implements Specification<Order> {

    private final OrderRepository orderRepository;

    /**
     * 客户标识
     */
    private String customerIdentity;

    /**
     * 车型编码
     */
    private String modelCode;

    /**
     * 订单类型
     */
    private OrderType orderType;

    @Override
    public boolean isSatisfiedBy(Order candidate) {
        // 提取客户标识（手机号哈希或身份证哈希）
        this.customerIdentity = extractCustomerIdentity(candidate);
        
        if (customerIdentity == null || customerIdentity.isEmpty()) {
            return true; // 无法识别客户，认为不重复
        }

        if (orderType == OrderType.SMALL) {
            // 小订单：同客户同车型重复控制
            this.modelCode = candidate.getVehicleInfo() != null 
                ? candidate.getVehicleInfo().getModelCode() 
                : null;
            return !existsSmallOrder();
        } else if (orderType == OrderType.FORMAL) {
            // 正式订单：客户维度重复控制
            return !existsFormalOrder();
        }

        return true;
    }

    /**
     * 提取客户标识
     */
    private String extractCustomerIdentity(Order order) {
        if (order.getCustomerInfo() == null) {
            return null;
        }
        // 优先使用身份证号哈希，否则使用手机号哈希，最后使用用户 ID
        if (order.getCustomerInfo().getIdNoHash() != null) {
            return order.getCustomerInfo().getIdNoHash();
        }
        if (order.getCustomerInfo().getMobileHash() != null) {
            return order.getCustomerInfo().getMobileHash();
        }
        return order.getCustomerInfo().getUserId();
    }

    /**
     * 检查是否存在有效小订单
     */
    private boolean existsSmallOrder() {
        if (modelCode == null) {
            return false;
        }
        // TODO: 调用 Repository 查询同客户同车型的有效小订单
        // 有效状态：除已关闭、已取消、已完成外的状态
        return false;
    }

    /**
     * 检查是否存在有效正式订单
     */
    private boolean existsFormalOrder() {
        // TODO: 调用 Repository 查询同客户的有效正式订单
        // 有效状态：除已关闭、已取消、已完成外的状态
        return false;
    }

    /**
     * 设置订单类型
     */
    public DuplicateOrderSpecification withOrderType(OrderType orderType) {
        this.orderType = orderType;
        return this;
    }

    /**
     * 设置车型编码（小订单用）
     */
    public DuplicateOrderSpecification withModelCode(String modelCode) {
        this.modelCode = modelCode;
        return this;
    }

}
