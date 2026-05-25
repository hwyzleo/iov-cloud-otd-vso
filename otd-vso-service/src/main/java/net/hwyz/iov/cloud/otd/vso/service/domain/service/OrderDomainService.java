package net.hwyz.iov.cloud.otd.vso.service.domain.service;

import net.hwyz.iov.cloud.otd.vso.service.domain.model.Order;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderState;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderAmount;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderAmountRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.shared.CustomerInfo;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.shared.VehicleInfo;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.shared.OrganizationInfo;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.AuditRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderAmountPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderTimelinePo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import cn.hutool.core.util.IdUtil;
import java.time.LocalDateTime;

/**
 * 订单领域服务
 *
 * @author VSO Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderDomainService {

    private final OrderRepository orderRepository;
    private final OrderAmountRepository orderAmountRepository;
    private final AuditRepository auditRepository;

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
        
        // 记录时间线
        saveTimeline(orderId, "ORDER_CREATE", "创建小订单", null, "EARNEST_MONEY_UNPAID",
                "system", "system", orderSource, null, "success", null, "小订单创建成功");
        
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
        
        // 记录时间线
        saveTimeline(orderId, "ORDER_CREATE", "创建正式订单", null, "PENDING_SUBMIT",
                "system", "system", orderSource, null, "success", null, "正式订单创建成功");
        
        return order;
    }

    /**
     * 提交订单
     */
    @Transactional(rollbackFor = Exception.class)
    public void submitOrder(String orderId) {
        Order order = loadOrder(orderId);
        Integer beforeStatus = order.getOrderState() != null ? order.getOrderState().getValue() : null;
        order.submit();
        saveOrder(order);
        
        saveTimeline(orderId, "ORDER_SUBMIT", "提交审核", 
                String.valueOf(beforeStatus), "PENDING_AUDIT",
                "system", "system", "system", null, "success", null, "订单已提交审核");
    }

    /**
     * 审核通过
     */
    @Transactional(rollbackFor = Exception.class)
    public void auditPass(String orderId) {
        Order order = loadOrder(orderId);
        Integer beforeStatus = order.getOrderState() != null ? order.getOrderState().getValue() : null;
        order.auditPass();
        saveOrder(order);
        
        saveTimeline(orderId, "AUDIT_PASS", "审核通过", 
                String.valueOf(beforeStatus),
                order.getOrderState() != null ? String.valueOf(order.getOrderState().getValue()) : null,
                "system", "auditor", "system", null, "success", null, "订单审核通过");
    }

    /**
     * 审核驳回
     */
    @Transactional(rollbackFor = Exception.class)
    public void auditReject(String orderId, String reason) {
        Order order = loadOrder(orderId);
        Integer beforeStatus = order.getOrderState() != null ? order.getOrderState().getValue() : null;
        order.auditReject(reason);
        saveOrder(order);
        
        saveTimeline(orderId, "AUDIT_REJECT", "审核驳回",
                String.valueOf(beforeStatus),
                order.getOrderState() != null ? String.valueOf(order.getOrderState().getValue()) : null,
                "system", "auditor", "system", null, "success", reason, "订单审核驳回");
    }

    /**
     * 锁单
     */
    @Transactional(rollbackFor = Exception.class)
    public void lockOrder(String orderId) {
        Order order = loadOrder(orderId);
        Integer beforeStatus = order.getOrderState() != null ? order.getOrderState().getValue() : null;
        order.lock();
        saveOrder(order);
        
        saveTimeline(orderId, "ORDER_LOCK", "锁单",
                String.valueOf(beforeStatus),
                order.getOrderState() != null ? String.valueOf(order.getOrderState().getValue()) : null,
                "system", "system", "system", null, "success", null, "订单已锁单");
    }

    /**
     * 取消订单
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(String orderId, String reason) {
        Order order = loadOrder(orderId);
        Integer beforeStatus = order.getOrderState() != null ? order.getOrderState().getValue() : null;
        order.cancel(reason);
        saveOrder(order);
        
        saveTimeline(orderId, "ORDER_CANCEL", "取消订单",
                String.valueOf(beforeStatus),
                order.getOrderState() != null ? String.valueOf(order.getOrderState().getValue()) : null,
                "system", "system", "system", null, "success", reason, "订单已取消");
    }

    /**
     * 关闭订单
     */
    @Transactional(rollbackFor = Exception.class)
    public void closeOrder(String orderId, String reason) {
        Order order = loadOrder(orderId);
        Integer beforeStatus = order.getOrderState() != null ? order.getOrderState().getValue() : null;
        order.close(reason);
        saveOrder(order);
        
        saveTimeline(orderId, "ORDER_CLOSE", "关闭订单",
                String.valueOf(beforeStatus),
                order.getOrderState() != null ? String.valueOf(order.getOrderState().getValue()) : null,
                "system", "system", "system", null, "success", reason, "订单已关闭");
    }

    /**
     * 完成订单
     */
    @Transactional(rollbackFor = Exception.class)
    public void completeOrder(String orderId) {
        Order order = loadOrder(orderId);
        Integer beforeStatus = order.getOrderState() != null ? order.getOrderState().getValue() : null;
        order.complete();
        saveOrder(order);
        
        saveTimeline(orderId, "ORDER_COMPLETE", "订单完成",
                String.valueOf(beforeStatus),
                order.getOrderState() != null ? String.valueOf(order.getOrderState().getValue()) : null,
                "system", "system", "system", null, "success", null, "订单已完成");
    }

    /**
     * VIN占用超时释放，订单状态回退
     */
    public void unassignVehicle(String orderId) {
        Order order = loadOrder(orderId);
        if (order.getOrderState() == OrderState.ALLOCATION_VEHICLE) {
            order.unassignVehicle();
            orderRepository.save(order);
        }
    }

    /**
     * 失效小订单（超时）
     */
    @Transactional(rollbackFor = Exception.class)
    public void invalidateSmallOrder(String orderId) {
        Order order = loadOrder(orderId);
        Integer beforeStatus = order.getOrderState() != null ? order.getOrderState().getValue() : null;
        order.invalidate();
        saveOrder(order);
        
        saveTimeline(orderId, "ORDER_INVALIDATE", "小订单失效",
                String.valueOf(beforeStatus),
                order.getOrderState() != null ? String.valueOf(order.getOrderState().getValue()) : null,
                "system", "system", "system", null, "success", null, "小订单超时失效");
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
        po.setOrderType(order.getOrderType() != null ? order.getOrderType().name().toLowerCase() : null);
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

    /**
     * 保存时间线记录
     */
    private void saveTimeline(String orderId, String eventType, String eventName,
                              String beforeStatus, String afterStatus,
                              String operatorId, String operatorRole, String operateSource,
                              String relatedDocNo, String result, String failReason, String eventRemark) {
        OrderTimelinePo timelinePo = new OrderTimelinePo();
        timelinePo.setTimelineId(IdUtil.fastSimpleUUID());
        timelinePo.setOrderId(orderId);
        timelinePo.setEventType(eventType);
        timelinePo.setEventName(eventName);
        timelinePo.setBeforeStatus(beforeStatus);
        timelinePo.setAfterStatus(afterStatus);
        timelinePo.setOperatorId(operatorId);
        timelinePo.setOperatorRole(operatorRole);
        timelinePo.setOperateSource(operateSource);
        timelinePo.setRelatedDocNo(relatedDocNo);
        timelinePo.setResult(result);
        timelinePo.setFailReason(failReason);
        timelinePo.setEventRemark(eventRemark);
        timelinePo.setEventTime(LocalDateTime.now());
        auditRepository.saveTimeline(timelinePo);
        log.debug("保存时间线记录：orderId={}, eventType={}", orderId, eventType);
    }

}
