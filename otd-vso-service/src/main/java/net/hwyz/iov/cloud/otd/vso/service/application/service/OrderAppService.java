package net.hwyz.iov.cloud.otd.vso.service.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
//import net.hwyz.iov.cloud.dms.org.api.feign.service.ExDealershipStaffService;
import net.hwyz.iov.cloud.edd.vmd.api.service.VmdVehicleModelConfigService;
import net.hwyz.iov.cloud.edd.vmd.api.vo.response.VmdBuildConfigResponse;
import net.hwyz.iov.cloud.framework.web.util.PageUtil;
import net.hwyz.iov.cloud.otd.vso.service.adapter.web.assembler.OrderDtoAssembler;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.*;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.query.CountQuery;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.query.OrderQuery;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.*;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.BrandCodeNotExistException;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.BuildConfigNotMatchedException;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.OrderNotExistException;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.WishlistNotExistException;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Order;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderAmount;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderState;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Wishlist;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.shared.CustomerInfo;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.shared.OrganizationInfo;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.shared.VehicleInfo;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.WishlistRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.service.OrderDomainService;
import net.hwyz.iov.cloud.otd.vso.service.domain.service.OrderLockService;
import net.hwyz.iov.cloud.otd.vso.service.domain.service.OrderPhysicalDeleteService;
import net.hwyz.iov.cloud.otd.vso.service.domain.service.OrderValidationService;
import net.hwyz.iov.cloud.otd.vso.service.domain.service.TimeoutNotifyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private final WishlistRepository wishlistRepository;
    private final VmdVehicleModelConfigService vmdVehicleModelConfigService;
    private final OrderPhysicalDeleteService orderPhysicalDeleteService;

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
     * 物理删除订单
     */
    @Transactional(rollbackFor = Exception.class)
    public PhysicalDeleteResult deleteOrder(DeleteOrderCmd cmd) {
        log.info("物理删除订单：orderId={}, operatorId={}, reason={}",
                cmd.getOrderId(), cmd.getOperatorId(), cmd.getReason());

        return orderPhysicalDeleteService.physicalDeleteOrder(
                cmd.getOrderId(),
                cmd.getReason(),
                cmd.getOperatorId(),
                cmd.getComplianceFlag() != null ? cmd.getComplianceFlag() : false
        );
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
                query.getOrderNo(),
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
    public OrderDetailResult getByOrderNo(String orderNo) {
        if (orderNo == null || orderNo.isEmpty()) {
            throw new OrderNotExistException(orderNo);
        }
        Optional<Order> orderOpt = orderRepository.findByOrderNo(orderNo);
        if (orderOpt.isEmpty()) {
            throw new OrderNotExistException(orderNo);
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
        log.info("分派交付人员：orderId={}, deliveryPersonId={}", cmd.getOrderNo(), cmd.getDeliveryPersonId());
        Order order = orderDomainService.loadOrder(cmd.getOrderNo());
        order.saveDeliveryPerson(cmd.getDeliveryPersonId(), cmd.getDeliveryPersonName());
        orderRepository.save(order);
    }

    /**
     * 分派车辆
     */
    @Transactional(rollbackFor = Exception.class)
    public void assignVehicle(AssignVehicleCmd cmd) {
        log.info("分派车辆：orderId={}, vin={}", cmd.getOrderNo(), cmd.getVin());
        Order order = orderDomainService.loadOrder(cmd.getOrderNo());
        order.saveDeliveryVehicle(cmd.getVin());
        orderRepository.save(order);
    }

    /**
     * 申请运输
     */
    @Transactional(rollbackFor = Exception.class)
    public void applyTransport(ApplyTransportCmd cmd) {
        log.info("申请运输：orderId={}", cmd.getOrderNo());
        Order order = orderDomainService.loadOrder(cmd.getOrderNo());
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

    public String earnestMoneyOrder(EarnestMoneyCmd cmd) {
        log.info("意向金下单：accountId={}, saleCode={}, regionCode={}, featureConfig={}, wishlistId={}", 
                cmd.getAccountId(), cmd.getSaleCode(), cmd.getRegionCode(), cmd.getFeatureConfig(), cmd.getWishlistId());
        
        Order order = createOrFindOrder(cmd.getAccountId(), cmd.getOrderNo());
        
        String buildConfigCode = null;
        String saleCode = cmd.getSaleCode();
        
        if (cmd.getWishlistId() != null && !cmd.getWishlistId().isEmpty()) {
            Wishlist wishlist = wishlistRepository.findByWishlistIdAndUserId(cmd.getWishlistId(), cmd.getAccountId())
                .orElseThrow(() -> new WishlistNotExistException(cmd.getWishlistId()));
            
            saleCode = cmd.getSaleCode() != null ? cmd.getSaleCode() : wishlist.getSaleCode();
            buildConfigCode = cmd.getBuildConfigCode() != null ? cmd.getBuildConfigCode() : wishlist.getBuildConfigCode();
            
            if (buildConfigCode == null || buildConfigCode.isEmpty()) {
                if (cmd.getFeatureConfig() != null && !cmd.getFeatureConfig().isEmpty()) {
                    Map<String, String> featureConfig = new HashMap<>(cmd.getFeatureConfig());
                    featureConfig.remove("BASE_MODEL");
                    log.info("调用VMD获取buildConfigCode，featureConfig={}", featureConfig);
                    buildConfigCode = vmdVehicleModelConfigService.getVehicleBuildConfigCode(featureConfig);
                    log.info("VMD返回buildConfigCode={}", buildConfigCode);
                }
            }
            
            if (buildConfigCode == null || buildConfigCode.isEmpty()) {
                throw new BuildConfigNotMatchedException(saleCode);
            }
            
            order.saveBuildConfig(buildConfigCode, cmd.getModelConfigMap());
            VmdBuildConfigResponse buildConfig = vmdVehicleModelConfigService.getBuildConfigByCode(buildConfigCode);
            log.info("VMD返回buildConfig详情={}", buildConfig);
            if (buildConfig == null || buildConfig.getBrandCode() == null) {
                throw new BrandCodeNotExistException(buildConfigCode);
            }
            order.saveBrandCode(buildConfig.getBrandCode());
            order.saveRegionCode(cmd.getRegionCode());
            order.saveSaleCode(saleCode);
        } else {
            if (cmd.getFeatureConfig() != null && !cmd.getFeatureConfig().isEmpty()) {
                Map<String, String> featureConfig = new HashMap<>(cmd.getFeatureConfig());
                featureConfig.remove("BASE_MODEL");
                log.info("调用VMD获取buildConfigCode，featureConfig={}", featureConfig);
                buildConfigCode = vmdVehicleModelConfigService.getVehicleBuildConfigCode(featureConfig);
                log.info("VMD返回buildConfigCode={}", buildConfigCode);
            } else if (cmd.getBuildConfigCode() != null) {
                buildConfigCode = cmd.getBuildConfigCode();
            }
            
            if (buildConfigCode == null || buildConfigCode.isEmpty()) {
                throw new BuildConfigNotMatchedException(cmd.getSaleCode());
            }
            
            order.saveBuildConfig(buildConfigCode, cmd.getModelConfigMap());
            VmdBuildConfigResponse buildConfig = vmdVehicleModelConfigService.getBuildConfigByCode(buildConfigCode);
            log.info("VMD返回buildConfig详情={}", buildConfig);
            if (buildConfig == null || buildConfig.getBrandCode() == null) {
                throw new BrandCodeNotExistException(buildConfigCode);
            }
            order.saveBrandCode(buildConfig.getBrandCode());
            order.saveRegionCode(cmd.getRegionCode());
            order.saveSaleCode(saleCode);
        }
        
        order.createSmallOrder();
        order.saveLicenseCity(cmd.getLicenseCityCode());
        orderRepository.save(order);
        log.info("意向金下单完成：orderId={}, smallOrderNo={}, buildConfigCode={}, regionCode={}", 
                order.getId(), order.getSmallOrderNo(), order.getBuildConfigCode(), order.getRegionCode());
        return order.getSmallOrderNo();
    }

    public String downPaymentOrder(DownPaymentCmd cmd) {
        Order order = createOrFindOrder(cmd.getAccountId(), cmd.getOrderNo());
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

    public OrderDetailResult getUserOrder(String accountId, String orderNo) {
        Order order = findOrderById(accountId, orderNo);
        return OrderDtoAssembler.INSTANCE.toOrderDetailResult(order);
    }

    public void cancel(CancelCmd cmd) {
        Order order = findOrderById(cmd.getAccountId(), cmd.getOrderNo());
        order.cancel();
        orderRepository.save(order);
    }

    public PayResult pay(PayCmd cmd) {
        Order order = findOrderById(cmd.getAccountId(), cmd.getOrderNo());
        OrderState previousState = order.getOrderState();
        
        order.pay(cmd.getPaymentAmount());
        orderRepository.save(order);
        
        if (previousState == OrderState.EARNEST_MONEY_UNPAID && order.getOrderState() == OrderState.EARNEST_MONEY_PAID) {
            wishlistRepository.deleteByUserId(cmd.getAccountId());
            log.info("支付意向金成功后删除心愿单：accountId={}, orderNo={}", cmd.getAccountId(), cmd.getOrderNo());
        } else if (previousState == OrderState.DOWN_PAYMENT_UNPAID && order.getOrderState() == OrderState.DOWN_PAYMENT_PAID) {
            wishlistRepository.deleteByUserId(cmd.getAccountId());
            log.info("支付定金成功后删除心愿单：accountId={}, orderNo={}", cmd.getAccountId(), cmd.getOrderNo());
        }
        
        return PayResult.builder()
                .orderNo(order.getOrderNo())
//                .paymentMerchant(cmd.getPaymentMerchant())
//                .paymentReference(cmd.getPaymentReference())
                .paymentAmount(cmd.getPaymentAmount())
                .build();
    }

    public void requestRefund(RequestRefundCmd cmd) {
        Order order = findOrderById(cmd.getAccountId(), cmd.getOrderNo());
        order.requestRefund();
        orderRepository.save(order);
    }

    public void earnestMoneyToDownPayment(EarnestToDownCmd cmd) {
        Order order = findOrderById(cmd.getAccountId(), cmd.getOrderNo());
        order.earnestMoneyToDownPayment();
        orderRepository.save(order);
    }

    public void lock(LockCmd cmd) {
        Order order = findOrderById(cmd.getAccountId(), cmd.getOrderNo());
        order.lock();
        orderRepository.save(order);
    }

    public void prepareTransport(PrepareTransportCmd cmd) {
        Order order = findOrderByOrderNo(cmd.getOrderNo());
        order.prepareTransport();
        orderRepository.save(order);
    }

    public void transporting(TransportingCmd cmd) {
        Order order = findOrderByOrderNo(cmd.getOrderNo());
        order.transporting();
        orderRepository.save(order);
    }

    public void prepareDelivery(PrepareDeliveryCmd cmd) {
        Order order = findOrderByOrderNo(cmd.getOrderNo());
        order.prepareDelivery();
        orderRepository.save(order);
    }

    public void delivered(DeliveredCmd cmd) {
        Order order = findOrderByOrderNo(cmd.getOrderNo());
        order.delivered();
        orderRepository.save(order);
    }

    public void activate(ActivateCmd cmd) {
        Order order = findOrderByOrderNo(cmd.getOrderNo());
        order.activate();
        orderRepository.save(order);
    }

    public boolean remove(String orderNo) {
        Order order = findOrderByOrderNo(orderNo);
        order.manageDelete();
        orderRepository.save(order);
        return true;
    }

    private Order findOrderById(String accountId, String orderNo) {
        Optional<Order> orderOpt = orderRepository.findByOrderNoAndAccountId(orderNo, accountId);
        if (orderOpt.isEmpty()) {
            throw new OrderNotExistException(orderNo);
        }
        return orderOpt.get();
    }

    private Order findOrderByOrderNo(String orderNo) {
        Optional<Order> orderOpt = orderRepository.findByOrderNo(orderNo);
        if (orderOpt.isEmpty()) {
            throw new OrderNotExistException(orderNo);
        }
        return orderOpt.get();
    }

    private Order createOrFindOrder(String accountId, String orderNo) {
        Optional<Order> orderOpt = orderRepository.findByOrderNoAndAccountId(orderNo, accountId);
        if (orderOpt.isPresent()) {
            return orderOpt.get();
        }
        return Order.fromWishlist(accountId, null);
    }

}
