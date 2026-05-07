package net.hwyz.iov.cloud.otd.vso.service.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.dms.org.api.feign.service.ExDealershipStaffService;
import net.hwyz.iov.cloud.framework.web.util.PageUtil;
import net.hwyz.iov.cloud.otd.vso.service.adapter.web.assembler.OrderDtoAssembler;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.*;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.query.CountQuery;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.query.OrderQuery;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.*;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.OrderNotExistException;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Order;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderAmount;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderState;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.shared.CustomerInfo;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.shared.OrganizationInfo;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.shared.VehicleInfo;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.service.OrderDomainService;
import net.hwyz.iov.cloud.otd.vso.service.domain.service.OrderLockService;
import net.hwyz.iov.cloud.otd.vso.service.domain.service.OrderValidationService;
import net.hwyz.iov.cloud.otd.vso.service.domain.service.TimeoutNotifyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 车辆销售订单应用服务
 *
 * @author VSO Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderAppService {

    private final OrderDomainService orderDomainService;
    private final OrderValidationService orderValidationService;
    private final OrderLockService orderLockService;
    private final TimeoutNotifyService timeoutNotifyService;
    private final OrderRepository orderRepository;

    /**
     * 创建小订单
     */
    @Transactional(rollbackFor = Exception.class)
    public OrderCreateResult createSmallOrder(CreateSmallOrderCmd cmd) {
        log.info("创建小订单：userId={}, modelCode={}", cmd.getUserId(), cmd.getModelCode());

        CustomerInfo customerInfo = new CustomerInfo(
                cmd.getUserId(),
                cmd.getName(),
                cmd.getMobileHash(),
                cmd.getIdNoHash(),
                "personal"
        );
        VehicleInfo vehicleInfo = new VehicleInfo(
                cmd.getModelCode(),
                cmd.getModelName(),
                cmd.getConfigCode(),
                cmd.getConfigName(),
                cmd.getColorCode(),
                cmd.getColorName()
        );

        Order order = orderDomainService.createSmallOrder(cmd.getOrderSource(), customerInfo, vehicleInfo);

        timeoutNotifyService.createTimeoutTask(order.getId(), "SMALL_ORDER_PAY_TIMEOUT", "invalid", 30);

        log.info("小订单创建成功：orderId={}", order.getId());
        return OrderCreateResult.builder()
                .orderId(order.getId())
                .orderNo(order.getOrderNo())
                .build();
    }

    /**
     * 创建正式订单
     */
    @Transactional(rollbackFor = Exception.class)
    public OrderCreateResult createFormalOrder(CreateFormalOrderCmd cmd) {
        log.info("创建正式订单：userId={}, modelCode={}", cmd.getUserId(), cmd.getModelCode());

        CustomerInfo customerInfo = new CustomerInfo(
                cmd.getUserId(),
                cmd.getName(),
                cmd.getMobileHash(),
                cmd.getIdNoHash(),
                "personal"
        );
        VehicleInfo vehicleInfo = new VehicleInfo(
                cmd.getModelCode(),
                cmd.getModelName(),
                cmd.getConfigCode(),
                cmd.getConfigName(),
                cmd.getColorCode(),
                cmd.getColorName()
        );
        OrganizationInfo orgInfo = new OrganizationInfo(
                cmd.getRegionCode(),
                cmd.getRegionName(),
                cmd.getStoreCode(),
                cmd.getStoreName(),
                cmd.getSalesCode(),
                cmd.getSalesName()
        );
        OrderAmount orderAmount = new OrderAmount("AMT" + System.currentTimeMillis());

        Order order = orderDomainService.createFormalOrder(
                cmd.getOrderSource(), customerInfo, vehicleInfo, orgInfo, orderAmount);

        timeoutNotifyService.createTimeoutTask(order.getId(), "FORMAL_ORDER_AUDIT_TIMEOUT", "remind", 1440);

        log.info("正式订单创建成功：orderId={}", order.getId());
        return OrderCreateResult.builder()
                .orderId(order.getId())
                .orderNo(order.getOrderNo())
                .build();
    }

    /**
     * 提交订单
     */
    @Transactional(rollbackFor = Exception.class)
    public void submitOrder(SubmitOrderCmd cmd) {
        log.info("提交订单：orderId={}, operatorId={}", cmd.getOrderId(), cmd.getOperatorId());

        orderLockService.executeWithLock(cmd.getOrderId(), cmd.getOperatorId(), "SUBMIT", () -> {
            Order order = orderDomainService.loadOrder(cmd.getOrderId());
            orderValidationService.validateForSubmit(order);
            orderDomainService.submitOrder(cmd.getOrderId());

            timeoutNotifyService.createTimeoutTask(cmd.getOrderId(), "AUDIT_TIMEOUT", "remind", 1440);
        });

        log.info("订单提交成功：orderId={}", cmd.getOrderId());
    }

    /**
     * 审核通过
     */
    @Transactional(rollbackFor = Exception.class)
    public void auditPass(AuditOrderCmd cmd) {
        log.info("审核通过：orderId={}, operatorId={}", cmd.getOrderId(), cmd.getOperatorId());

        orderDomainService.auditPass(cmd.getOrderId());

        timeoutNotifyService.createTimeoutTask(cmd.getOrderId(), "LOCK_TIMEOUT", "remind", 2880);

        log.info("订单审核通过：orderId={}", cmd.getOrderId());
    }

    /**
     * 审核驳回
     */
    @Transactional(rollbackFor = Exception.class)
    public void auditReject(AuditOrderCmd cmd) {
        log.info("审核驳回：orderId={}, operatorId={}, reason={}",
                cmd.getOrderId(), cmd.getOperatorId(), cmd.getRejectReason());

        orderDomainService.auditReject(cmd.getOrderId(), cmd.getRejectReason());

        log.info("订单审核驳回：orderId={}", cmd.getOrderId());
    }

    /**
     * 锁单
     */
    @Transactional(rollbackFor = Exception.class)
    public void lockOrder(LockOrderCmd cmd) {
        log.info("锁单：orderId={}, operatorId={}", cmd.getOrderId(), cmd.getOperatorId());

        orderLockService.executeWithLock(cmd.getOrderId(), cmd.getOperatorId(), "LOCK", () -> {
            Order order = orderDomainService.loadOrder(cmd.getOrderId());
            orderValidationService.validateForLock(order);
            orderDomainService.lockOrder(cmd.getOrderId());
        });

        log.info("订单锁单成功：orderId={}", cmd.getOrderId());
    }

    /**
     * 取消/关闭订单 (新)
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(CancelOrderCmd cmd) {
        log.info("取消/关闭订单：orderId={}, operatorId={}, reason={}, type={}",
                cmd.getOrderId(), cmd.getOperatorId(), cmd.getReason(), cmd.getOperateType());

        orderLockService.executeWithLock(cmd.getOrderId(), cmd.getOperatorId(), cmd.getOperateType(), () -> {
            if ("CANCEL".equals(cmd.getOperateType())) {
                orderDomainService.cancelOrder(cmd.getOrderId(), cmd.getReason());
            } else if ("CLOSE".equals(cmd.getOperateType())) {
                orderDomainService.closeOrder(cmd.getOrderId(), cmd.getReason());
            }
        });

        log.info("订单取消/关闭成功：orderId={}", cmd.getOrderId());
    }

    /**
     * 完成订单
     */
    @Transactional(rollbackFor = Exception.class)
    public void completeOrder(CompleteOrderCmd cmd) {
        log.info("完成订单：orderId={}, operatorId={}", cmd.getOrderId(), cmd.getOperatorId());

        orderLockService.executeWithLock(cmd.getOrderId(), cmd.getOperatorId(), "COMPLETE", () -> {
            orderDomainService.completeOrder(cmd.getOrderId());
        });

        log.info("订单完成成功：orderId={}", cmd.getOrderId());
    }

    /**
     * 搜索订单
     */
    public List<OrderListResult> search(OrderQuery query) {
        if (query == null) {
            return new ArrayList<>();
        }
        List<OrderState> orderStateList = query.getOrderStateRange() != null
                ? query.getOrderStateRange().stream().map(OrderState::fromValue).collect(Collectors.toList())
                : null;
        List<Order> orderList = orderRepository.search(
                query.getOrderNum(),
                query.getOrderState() != null ? OrderState.fromValue(query.getOrderState()) : null,
                orderStateList,
                query.getHasDeliveryPerson(),
                query.getBeginTime(),
                query.getEndTime()
        );
        return PageUtil.convert(orderList, OrderDtoAssembler.INSTANCE::toOrderListResult);
    }

    /**
     * 按订单号获取详情
     */
    public OrderDetailResult getByOrderNum(String orderNum) {
        if (orderNum == null || orderNum.isEmpty()) {
            throw new OrderNotExistException(orderNum);
        }
        Optional<Order> orderOpt = orderRepository.findByOrderNum(orderNum);
        if (orderOpt.isEmpty()) {
            throw new OrderNotExistException(orderNum);
        }
        return OrderDtoAssembler.INSTANCE.toOrderDetailResult(orderOpt.get());
    }

    /**
     * 按 ID 获取详情
     */
    public OrderDetailResult getById(String orderId) {
        Order order = orderDomainService.loadOrder(orderId);
        return OrderDtoAssembler.INSTANCE.toOrderDetailResult(order);
    }

    /**
     * 统计订单数
     */
    public Integer count(CountQuery query) {
        if (query == null) {
            return 0;
        }
        return orderRepository.count(query.getDeliveryPersonId(), query.getDelivered());
    }

    /**
     * 分派交付人员
     */
    @Transactional(rollbackFor = Exception.class)
    public void assignDeliveryPerson(AssignDeliveryPersonCmd cmd) {
        log.info("分派交付人员：orderId={}, deliveryPersonId={}", cmd.getOrderNum(), cmd.getDeliveryPersonId());
        Order order = orderDomainService.loadOrder(cmd.getOrderNum());
        order.saveDeliveryPerson(cmd.getDeliveryPersonId(), cmd.getDeliveryPersonName());
        orderRepository.save(order);
    }

    /**
     * 分派车辆
     */
    @Transactional(rollbackFor = Exception.class)
    public void assignVehicle(AssignVehicleCmd cmd) {
        log.info("分派车辆：orderId={}, vin={}", cmd.getOrderNum(), cmd.getVin());
        Order order = orderDomainService.loadOrder(cmd.getOrderNum());
        order.saveDeliveryVehicle(cmd.getVin());
        orderRepository.save(order);
    }

    /**
     * 申请运输
     */
    @Transactional(rollbackFor = Exception.class)
    public void applyTransport(ApplyTransportCmd cmd) {
        log.info("申请运输：orderId={}", cmd.getOrderNum());
        Order order = orderDomainService.loadOrder(cmd.getOrderNum());
//        order.applyTransportVehicle(cmd.getOperatorId(), cmd.getOperatorName());
        orderRepository.save(order);
    }

    /**
     * 搜索交付中心人员
     */
    public List<DeliveryStaffResult> searchDeliveryStaff(String dealershipCode) {
//        var page = exDealershipStaffService.search(dealershipCode);
//        return PageUtil.convert(page, staff -> DeliveryStaffResult.builder()
//                .dealershipCode(staff.getDealershipCode())
//                .dealershipName(staff.getDealershipName())
//                .userId(staff.getUserId())
//                .userName(staff.getUserName())
//                .nickName(staff.getNickName())
//                .phonenumber(staff.getPhonenumber())
//                .notDeliveryOrderCount(count(CountQuery.builder()
//                        .deliveryPersonId(staff.getUserId().toString())
//                        .delivered(false)
//                        .build()))
//                .build());
        return new ArrayList<>();
    }

    // --- 以下为兼容旧接口的方法 ---

    public String createUserWishlist(CreateWishlistCmd cmd) {
        Order order = Order.fromWishlist(cmd.getAccountId(), cmd.getMobile(), cmd.getSaleCode());
        order.saveBuildConfig(cmd.getBuildConfigCode(), cmd.getModelConfigMap());
        order.saveLicenseCity(cmd.getLicenseCityCode());
        orderRepository.save(order);
        return order.getOrderNo();
    }

    public void modifyUserWishlist(ModifyWishlistCmd cmd) {
        Order order = findOrderById(cmd.getAccountId(), cmd.getOrderNum());
        order.saveBuildConfig(cmd.getBuildConfigCode(), cmd.getModelConfigMap());
        orderRepository.save(order);
    }

    public void deleteUserWishlist(DeleteWishlistCmd cmd) {
        Order order = findOrderById(cmd.getAccountId(), cmd.getOrderNum());
        order.markDelete();
        orderRepository.save(order);
    }

    public WishlistDetailResult getUserWishlist(String accountId, String orderNum) {
        Order order = findOrderById(accountId, orderNum);
        return OrderDtoAssembler.INSTANCE.toWishlistDetailResult(order);
    }

    public String earnestMoneyOrder(EarnestMoneyCmd cmd) {
        Order order = createOrFindOrder(cmd.getAccountId(), cmd.getOrderNum());
        order.earnestMoneyOrder();
        order.saveBuildConfig(cmd.getBuildConfigCode(), cmd.getModelConfigMap());
        order.saveLicenseCity(cmd.getLicenseCityCode());
        orderRepository.save(order);
        return order.getOrderNo();
    }

    public String downPaymentOrder(DownPaymentCmd cmd) {
        Order order = createOrFindOrder(cmd.getAccountId(), cmd.getOrderNum());
        order.downPaymentOrder();
        order.saveBuildConfig(cmd.getBuildConfigCode(), cmd.getModelConfigMap());
        order.saveOrderPerson(cmd.getAccountId(), cmd.getOrderPersonType(), cmd.getOrderPersonName(),
                cmd.getOrderPersonIdType(), cmd.getOrderPersonIdNum());
        order.savePurchasePlan(cmd.getPurchasePlan());
        order.saveLicenseCity(cmd.getLicenseCityCode());
        order.saveDealership(cmd.getDealership());
        order.saveDeliveryCenter(cmd.getDeliveryCenter());
        orderRepository.save(order);
        return order.getOrderNo();
    }

    public OrderDetailResult getUserOrder(String accountId, String orderNum) {
        Order order = findOrderById(accountId, orderNum);
        return OrderDtoAssembler.INSTANCE.toOrderDetailResult(order);
    }

    public void cancel(CancelCmd cmd) {
        Order order = findOrderById(cmd.getAccountId(), cmd.getOrderNum());
        order.cancel();
        orderRepository.save(order);
    }

    public PayResult pay(PayCmd cmd) {
        Order order = findOrderById(cmd.getAccountId(), cmd.getOrderNum());
        order.pay(cmd.getPaymentAmount());
        orderRepository.save(order);
        return PayResult.builder()
                .orderNum(order.getOrderNo())
//                .paymentMerchant(cmd.getPaymentMerchant())
//                .paymentReference(cmd.getPaymentReference())
                .paymentAmount(cmd.getPaymentAmount())
                .build();
    }

    public void requestRefund(RequestRefundCmd cmd) {
        Order order = findOrderById(cmd.getAccountId(), cmd.getOrderNum());
        order.requestRefund();
        orderRepository.save(order);
    }

    public void earnestMoneyToDownPayment(EarnestToDownCmd cmd) {
        Order order = findOrderById(cmd.getAccountId(), cmd.getOrderNum());
        order.earnestMoneyToDownPayment();
        orderRepository.save(order);
    }

    public void lock(LockCmd cmd) {
        Order order = findOrderById(cmd.getAccountId(), cmd.getOrderNum());
        order.lock();
        orderRepository.save(order);
    }

    public void prepareTransport(PrepareTransportCmd cmd) {
        Order order = findOrderByOrderNum(cmd.getOrderNum());
        order.prepareTransport();
        orderRepository.save(order);
    }

    public void transporting(TransportingCmd cmd) {
        Order order = findOrderByOrderNum(cmd.getOrderNum());
        order.transporting();
        orderRepository.save(order);
    }

    public void prepareDelivery(PrepareDeliveryCmd cmd) {
        Order order = findOrderByOrderNum(cmd.getOrderNum());
        order.prepareDelivery();
        orderRepository.save(order);
    }

    public void delivered(DeliveredCmd cmd) {
        Order order = findOrderByOrderNum(cmd.getOrderNum());
        order.delivered();
        orderRepository.save(order);
    }

    public void activate(ActivateCmd cmd) {
        Order order = findOrderByOrderNum(cmd.getOrderNum());
        order.activate();
        orderRepository.save(order);
    }

    public boolean remove(String orderNum) {
        Order order = findOrderByOrderNum(orderNum);
        order.manageDelete();
        orderRepository.save(order);
        return true;
    }

    private Order findOrderById(String accountId, String orderNum) {
        Optional<Order> orderOpt = orderRepository.findByOrderNumAndAccountId(orderNum, accountId);
        if (orderOpt.isEmpty()) {
            throw new OrderNotExistException(orderNum);
        }
        return orderOpt.get();
    }

    private Order findOrderByOrderNum(String orderNum) {
        Optional<Order> orderOpt = orderRepository.findByOrderNum(orderNum);
        if (orderOpt.isEmpty()) {
            throw new OrderNotExistException(orderNum);
        }
        return orderOpt.get();
    }

    private Order createOrFindOrder(String accountId, String orderNum) {
        Optional<Order> orderOpt = orderRepository.findByOrderNumAndAccountId(orderNum, accountId);
        if (orderOpt.isPresent()) {
            return orderOpt.get();
        }
        return Order.fromWishlist(accountId, null, null);
    }

}
