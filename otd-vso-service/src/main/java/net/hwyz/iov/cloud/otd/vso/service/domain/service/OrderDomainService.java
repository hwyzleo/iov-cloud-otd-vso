package net.hwyz.iov.cloud.otd.vso.service.domain.service;

import net.hwyz.iov.cloud.otd.vso.service.domain.model.Order;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderAmount;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderAmountRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.shared.CustomerInfo;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.shared.VehicleInfo;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.shared.OrganizationInfo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderAmountPo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 订单领域服务
 *
 * @author VSO Team
 */
@Service
@RequiredArgsConstructor
public class OrderDomainService {

    private final OrderRepository orderRepository;
    private final OrderAmountRepository orderAmountRepository;

    /**
     * 创建小订单
     */
    @Transactional(rollbackFor = Exception.class)
    public Order createSmallOrder(String orderSource, CustomerInfo customerInfo, VehicleInfo vehicleInfo) {
        // 生成订单 ID
        String orderId = generateOrderId();
        
        // 创建订单聚合根
        Order order = Order.createSmallOrder(orderId, orderSource);
        
        // 设置客户信息
        order.setCustomerInfo(customerInfo);
        
        // 设置车辆信息
        order.setVehicleInfo(vehicleInfo);
        
        // 初始化订单金额
        OrderAmount orderAmount = new OrderAmount(generateAmountId());
        order.setOrderAmount(orderAmount);
        
        // 保存订单
        saveOrder(order);
        
        return order;
    }

    /**
     * 创建正式订单
     */
    @Transactional(rollbackFor = Exception.class)
    public Order createFormalOrder(String orderSource, CustomerInfo customerInfo, 
                                   VehicleInfo vehicleInfo, OrganizationInfo organizationInfo,
                                   OrderAmount orderAmount) {
        // 生成订单 ID
        String orderId = generateOrderId();
        
        // 创建订单聚合根
        Order order = Order.createFormalOrder(orderId, orderSource);
        
        // 设置信息
        order.setCustomerInfo(customerInfo);
        order.setVehicleInfo(vehicleInfo);
        order.setOrganizationInfo(organizationInfo);
        order.setOrderAmount(orderAmount);
        
        // 保存订单
        saveOrder(order);
        
        return order;
    }

    /**
     * 提交订单
     */
    @Transactional(rollbackFor = Exception.class)
    public void submitOrder(String orderId) {
        Order order = loadOrder(orderId);
        order.submit();
        saveOrder(order);
    }

    /**
     * 审核通过
     */
    @Transactional(rollbackFor = Exception.class)
    public void auditPass(String orderId) {
        Order order = loadOrder(orderId);
        order.auditPass();
        saveOrder(order);
    }

    /**
     * 审核驳回
     */
    @Transactional(rollbackFor = Exception.class)
    public void auditReject(String orderId, String reason) {
        Order order = loadOrder(orderId);
        order.auditReject(reason);
        saveOrder(order);
    }

    /**
     * 锁单
     */
    @Transactional(rollbackFor = Exception.class)
    public void lockOrder(String orderId) {
        Order order = loadOrder(orderId);
        order.lock();
        saveOrder(order);
    }

    /**
     * 取消订单
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(String orderId, String reason) {
        Order order = loadOrder(orderId);
        order.cancel(reason);
        saveOrder(order);
    }

    /**
     * 关闭订单
     */
    @Transactional(rollbackFor = Exception.class)
    public void closeOrder(String orderId, String reason) {
        Order order = loadOrder(orderId);
        order.close(reason);
        saveOrder(order);
    }

    /**
     * 完成订单
     */
    @Transactional(rollbackFor = Exception.class)
    public void completeOrder(String orderId) {
        Order order = loadOrder(orderId);
        order.complete();
        saveOrder(order);
    }

    /**
     * 加载订单
     */
    public Order loadOrder(String orderId) {
        return orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("订单不存在：" + orderId));
    }

    /**
     * 保存订单
     */
    private void saveOrder(Order order) {
        // 保存订单
        orderRepository.save(order);
        
        // 保存订单金额
        if (order.getOrderAmount() != null) {
            OrderAmountPo amountPo = convertAmountToPo(order.getOrderAmount(), order.getId());
            orderAmountRepository.save(amountPo);
        }
    }

    /**
     * 转换为 PO
     */
    private OrderPo convertToPo(Order order) {
        OrderPo po = new OrderPo();
        po.setOrderId(order.getId());
        po.setOrderNo(order.getOrderNo());
        po.setSmallOrderNo(order.getSmallOrderNo());
        po.setOrderType(order.getOrderType());
        po.setOrderSource(order.getOrderSource());
        po.setRowValid(order.getValid() != null && order.getValid());
        return po;
    }

    /**
     * 金额转换为 PO
     */
    private OrderAmountPo convertAmountToPo(OrderAmount orderAmount, String orderId) {
        OrderAmountPo po = new OrderAmountPo();
        po.setAmountId(orderAmount.getAmountId());
        po.setOrderId(orderId);
        po.setGuidePrice(orderAmount.getGuidePrice().getAmount());
        po.setVehiclePrice(orderAmount.getVehiclePrice().getAmount());
        po.setOptionPrice(orderAmount.getOptionPrice().getAmount());
        po.setColorMarkup(orderAmount.getColorMarkup().getAmount());
        po.setServiceFee(orderAmount.getServiceFee().getAmount());
        po.setPlateServiceFee(orderAmount.getPlateServiceFee().getAmount());
        po.setInsuranceFee(orderAmount.getInsuranceFee().getAmount());
        po.setDiscountTotal(orderAmount.getDiscountTotal().getAmount());
        po.setSubsidyTotal(orderAmount.getSubsidyTotal().getAmount());
        po.setFinanceDiscountTotal(orderAmount.getFinanceDiscountTotal().getAmount());
        po.setDealPriceTotal(orderAmount.getDealPriceTotal().getAmount());
        po.setDepositAmount(orderAmount.getDepositAmount().getAmount());
        po.setDownPaymentAmount(orderAmount.getDownPaymentAmount().getAmount());
        po.setTailPaymentAmount(orderAmount.getTailPaymentAmount().getAmount());
        po.setPaidTotal(orderAmount.getPaidTotal().getAmount());
        po.setRefundTotal(orderAmount.getRefundTotal().getAmount());
        po.setReceivableTotal(orderAmount.getReceivableTotal().getAmount());
        po.setNetReceivableTotal(orderAmount.getNetReceivableTotal().getAmount());
        po.setUnpaidTotal(orderAmount.getUnpaidTotal().getAmount());
        po.setInvoiceAmount(orderAmount.getInvoiceAmount().getAmount());
        po.setCalculationVersion(orderAmount.getCalculationVersion());
        po.setRowValid(1);
        return po;
    }

    /**
     * 生成订单 ID
     */
    private String generateOrderId() {
        // TODO: 实现订单号生成逻辑
        return "ORD" + System.currentTimeMillis();
    }

    /**
     * 生成金额 ID
     */
    private String generateAmountId() {
        // TODO: 实现金额 ID 生成逻辑
        return "AMT" + System.currentTimeMillis();
    }

}
