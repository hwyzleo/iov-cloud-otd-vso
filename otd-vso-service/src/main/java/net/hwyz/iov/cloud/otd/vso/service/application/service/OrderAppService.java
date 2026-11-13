package net.hwyz.iov.cloud.otd.vso.service.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.edd.dict.api.service.DictionaryService;
import net.hwyz.iov.cloud.edd.dict.api.vo.response.DictionaryResponse;
import net.hwyz.iov.cloud.edd.org.api.service.OrgDealershipService;
import net.hwyz.iov.cloud.edd.org.api.service.OrgDealershipStaffService;
import net.hwyz.iov.cloud.edd.org.api.vo.DealershipStaffExService;
import net.hwyz.iov.cloud.edd.org.api.vo.DealershipExService;
import net.hwyz.iov.cloud.edd.vmd.api.service.VmdVehicleModelConfigService;
import net.hwyz.iov.cloud.edd.vmd.api.vo.response.VmdBuildConfigResponse;
import net.hwyz.iov.cloud.edd.mdm.api.service.ConfigurationService;
import net.hwyz.iov.cloud.edd.mdm.api.vo.request.ConfigurationByVariantAndOptionCodesRequest;
import net.hwyz.iov.cloud.edd.mdm.api.vo.response.ConfigurationResponse;
import net.hwyz.iov.cloud.framework.common.bean.PageResult;
import net.hwyz.iov.cloud.framework.web.util.PageUtil;
import net.hwyz.iov.cloud.otd.vso.api.enums.CustomerType;
import net.hwyz.iov.cloud.otd.vso.api.enums.OrderType;
import net.hwyz.iov.cloud.otd.vso.api.enums.PaymentChannel;
import net.hwyz.iov.cloud.otd.vso.api.enums.PaymentMethod;
import net.hwyz.iov.cloud.otd.vso.api.enums.PaymentStage;
import net.hwyz.iov.cloud.otd.vso.api.enums.PaymentStatus;
import net.hwyz.iov.cloud.otd.vso.service.adapter.web.assembler.OrderDtoAssembler;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.*;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.ResubmitAuditCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.query.CountQuery;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.query.OrderQuery;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.*;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.BrandCodeNotExistException;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.BuildConfigNotMatchedException;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.ConfigurationNotMatchedException;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.OrderNotExistException;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.OrderAmountSnapshotMissingException;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.OrderReopenLimitExceededException;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.OrderReopenNotAllowedException;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.OrderStateNotAllowedException;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.PaymentChannelNotAvailableException;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.SaleModelNotExistException;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.WishlistNotExistException;
import net.hwyz.iov.cloud.otd.vso.service.domain.service.MdmProjectionService;
import net.hwyz.iov.cloud.otd.vso.service.domain.service.SalesPolicyService;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Order;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderAmount;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderState;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Payment;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderAgreementConfirmation;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderTimelineEventType;
import net.hwyz.iov.cloud.otd.vso.service.domain.service.OrderStateMachine;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.VehicleAssignment;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Wishlist;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.shared.CustomerInfo;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.shared.Money;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.shared.OrganizationInfo;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.shared.VehicleInfo;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderPartyRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.DeliveryAppointment;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.DeliveryRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderAssignmentRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderAmountRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderTimelineRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.PaymentRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.SaleModelRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.SaleModelModelPolicyRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.SaleModelOptionPolicyRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.SaleModelOptionFamilyPolicyRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.SaleModelVariantPolicyRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.WishlistRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.AuditRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderVehicleSnapshotRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.VehicleAssignmentRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.service.OrderDomainService;
import net.hwyz.iov.cloud.otd.vso.service.domain.service.OrderLockService;
import net.hwyz.iov.cloud.otd.vso.service.domain.service.OrderPhysicalDeleteService;
import net.hwyz.iov.cloud.otd.vso.service.domain.service.OrderValidationService;
import net.hwyz.iov.cloud.otd.vso.service.domain.service.TimeoutNotifyService;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.config.PaymentChannelConfig;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderAmountPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderAssignmentPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderPartyPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderTimelinePo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderVehicleSnapshotPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.PaymentPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.RefundPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelOptionPolicyPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.MdmProjectionOptionFamilyPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.MdmProjectionOptionPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelVariantPolicyPo;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.RefundRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.SupplementaryPaymentRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.ConfigChangeRefundRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.event.ConfigChangePriceDiffEvent;
import net.hwyz.iov.cloud.otd.vso.service.domain.gateway.PaymentAdapter;
import net.hwyz.iov.cloud.otd.vso.service.domain.gateway.VehicleInventoryGateway;
import net.hwyz.iov.cloud.otd.vso.service.domain.policy.DuplicateOrderSpecification;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SupplementaryPaymentPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.ConfigChangeRefundPo;
import net.hwyz.iov.cloud.otd.vso.api.enums.SupplementaryPaymentStatus;
import net.hwyz.iov.cloud.otd.vso.api.enums.SupplementaryPaymentScene;
import net.hwyz.iov.cloud.otd.vso.api.enums.ConfigChangeRefundStatus;
import net.hwyz.iov.cloud.otd.vso.api.vo.SupplementaryPaymentVo;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.SupplementPaymentNotExistException;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.SupplementPaymentStatusException;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.SupplementPaymentExpiredException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Async;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
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

    /**
     * 小订单允许的最大重新支付次数（OTD-VSO-DSN-CR-005 INV-013，连同首次下单共 3 次支付机会）
     */
    public static final int MAX_REOPEN_COUNT = 2;

    private final OrderDomainService orderDomainService;
    private final OrderValidationService orderValidationService;
    private final OrderLockService orderLockService;
    private final TimeoutNotifyService timeoutNotifyService;
    private final OrderRepository orderRepository;
    private final OrderAmountRepository orderAmountRepository;
    private final OrderPartyRepository orderPartyRepository;
    private final OrderAssignmentRepository orderAssignmentRepository;
    private final WishlistRepository wishlistRepository;
    private final VmdVehicleModelConfigService vmdVehicleModelConfigService;
    private final ConfigurationService configurationService;
    private final OrderPhysicalDeleteService orderPhysicalDeleteService;
    private final PaymentChannelConfig paymentChannelConfig;
    private final SaleModelRepository saleModelRepository;
    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;
    private final SaleModelAppService saleModelAppService;
    private final DictionaryService dictionaryService;
    private final OrgDealershipService orgDealershipService;
    private final OrgDealershipStaffService orgDealershipStaffService;
    private final AuditRepository auditRepository;
    private final OrderVehicleSnapshotRepository orderVehicleSnapshotRepository;
    private final SupplementaryPaymentRepository supplementaryPaymentRepository;
    private final ConfigChangeRefundRepository configChangeRefundRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final PaymentAdapter paymentAdapter;
    private final DuplicateOrderSpecification duplicateOrderSpecification;
    private final VehicleAssignmentRepository vehicleAssignmentRepository;
    private final VehicleInventoryGateway vehicleInventoryGateway;
    private final SalesPolicyService salesPolicyService;
    private final MdmProjectionService mdmProjectionService;
    private final SaleModelVariantPolicyRepository saleModelVariantPolicyRepository;
    private final SaleModelModelPolicyRepository saleModelModelPolicyRepository;
    private final SaleModelOptionPolicyRepository optionPolicyRepository;
    private final SaleModelOptionFamilyPolicyRepository optionFamilyPolicyRepository;
    private final PaymentAppService paymentAppService;
    private final RefundAppService refundAppService;
    private final DeliveryAppService deliveryAppService;
    private final DeliveryRepository deliveryRepository;
    private final OrderTimelineRepository orderTimelineRepository;
    private final PurchaseAgreementAppService purchaseAgreementAppService;
    private final OrderTimelineAppService orderTimelineAppService;
    private final VehicleAssignmentAppService vehicleAssignmentAppService;

    private final Map<String, String> cityNameCache = new ConcurrentHashMap<>();
    private volatile long cityNameCacheLastRefresh = 0;
    private static final long CITY_CACHE_TTL_MS = 5 * 60 * 1000;

    /**
     * 创建小订单
     */
    @Transactional(rollbackFor = Exception.class)
    public OrderCreateResult createSmallOrder(CreateSmallOrderCmd cmd) {
        log.info("创建小订单：userId={}, modelCode={}", cmd.getUserId(), cmd.getModelCode());

        validateOrderBeforeCreate(cmd.getSaleModelCode(), cmd.getVariantCode(), cmd.getOptionCodes(), cmd.getRegionCode());

        CustomerInfo customerInfo = new CustomerInfo(
                cmd.getUserId(),
                cmd.getName(),
                cmd.getMobileHash(),
                cmd.getIdNoHash(),
                CustomerType.PERSONAL.getCode()
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

        wishlistRepository.deleteByUserId(cmd.getUserId());
        log.info("小订单创建成功后删除心愿单：userId={}, orderNo={}", cmd.getUserId(), order.getOrderNo());

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

        validateOrderBeforeCreate(cmd.getSaleModelCode(), cmd.getVariantCode(), cmd.getOptionCodes(), cmd.getRegionCode());

        CustomerInfo customerInfo = new CustomerInfo(
                cmd.getUserId(),
                cmd.getName(),
                cmd.getMobileHash(),
                cmd.getIdNoHash(),
                CustomerType.PERSONAL.getCode()
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
                cmd.getOwnerRegionCode(),
                cmd.getOwnerRegionName(),
                cmd.getOwnerStoreCode(),
                cmd.getOwnerStoreName(),
                cmd.getSalesCode(),
                cmd.getSalesName()
        );

        // 获取 Variant 价格信息
        String saleModelCode = cmd.getSaleModelCode();
        String variantCode = cmd.getVariantCode();
        List<String> optionCodes = cmd.getOptionCodes() != null ? cmd.getOptionCodes() : new ArrayList<>();

        SaleModelVariantPolicyPo variantPolicy = saleModelVariantPolicyRepository
                .findBySaleModelCodeAndVariantCode(saleModelCode, variantCode)
                .orElse(null);

        BigDecimal variantPrice = BigDecimal.ZERO;
        if (variantPolicy != null && "active".equals(variantPolicy.getSaleStatus())) {
            variantPrice = variantPolicy.getVariantPrice() != null ? variantPolicy.getVariantPrice() : BigDecimal.ZERO;
        }

        // 计算 Option 总价
        BigDecimal optionTotalPrice = BigDecimal.ZERO;
        for (String optionCode : optionCodes) {
            BigDecimal optionPrice = salesPolicyService.getOptionPrice(saleModelCode, optionCode);
            optionTotalPrice = optionTotalPrice.add(optionPrice);
        }

        // 创建 OrderAmount 并设置价格
        OrderAmount orderAmount = new OrderAmount("AMT" + System.currentTimeMillis());
        orderAmount.setVehiclePrice(new Money(variantPrice));
        orderAmount.setOptionPrice(new Money(optionTotalPrice));
        // 计算成交总价
        orderAmount.calculateDealPrice();
        // 计算净应收和待支付
        orderAmount.recalculate();

        Order order = orderDomainService.createFormalOrder(
                cmd.getOrderSource(), customerInfo, vehicleInfo, orgInfo, orderAmount);

        timeoutNotifyService.createTimeoutTask(order.getId(), "FORMAL_ORDER_AUDIT_TIMEOUT", "remind", 1440);

        log.info("正式订单创建成功：orderId={}, vehiclePrice={}, optionPrice={}, dealPriceTotal={}",
                order.getId(), variantPrice, optionTotalPrice, orderAmount.getDealPriceTotal());
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

        orderLockService.executeWithLock(cmd.getOrderId(), cmd.getOperatorId(), "submit", () -> {
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
        log.info("审核驳回：orderId={}, operatorId={}, rejectCategory={}, reason={}",
                cmd.getOrderId(), cmd.getOperatorId(), cmd.getRejectCategory(), cmd.getRejectReason());

        orderDomainService.auditReject(cmd.getOrderId(), cmd.getRejectCategory(), cmd.getRejectReason());

        timeoutNotifyService.createTimeoutTask(cmd.getOrderId(), "AUDIT_REJECT_REMIND", "remind", 4320);
        timeoutNotifyService.createTimeoutTask(cmd.getOrderId(), "AUDIT_REJECT_TIMEOUT", "auto_close", 10080);

        log.info("订单审核驳回：orderId={}", cmd.getOrderId());
    }

    @Transactional(rollbackFor = Exception.class)
    public void resubmitAudit(ResubmitAuditCmd cmd) {
        log.info("重提审核：orderId={}, operatorId={}", cmd.getOrderId(), cmd.getOperatorId());

        orderDomainService.resubmitAudit(cmd.getOrderId());

        timeoutNotifyService.cancelByOrderIdAndType(cmd.getOrderId(), "AUDIT_REJECT_REMIND");
        timeoutNotifyService.cancelByOrderIdAndType(cmd.getOrderId(), "AUDIT_REJECT_TIMEOUT");
        timeoutNotifyService.createTimeoutTask(cmd.getOrderId(), "FORMAL_ORDER_AUDIT_TIMEOUT", "remind", 1440);

        log.info("订单重提审核成功：orderId={}", cmd.getOrderId());
    }

    /**
     * 锁单
     */
    @Transactional(rollbackFor = Exception.class)
    public void lockOrder(LockOrderCmd cmd) {
        log.info("锁单：orderId={}, operatorId={}", cmd.getOrderId(), cmd.getOperatorId());

        orderLockService.executeWithLock(cmd.getOrderId(), cmd.getOperatorId(), "lockOrder", () -> {
            Order order = orderDomainService.loadOrder(cmd.getOrderId());
            orderValidationService.validateForLock(order);
            orderDomainService.lockOrder(cmd.getOrderId());
            timeoutNotifyService.createTimeoutTask(cmd.getOrderId(), "LOCK_TIMEOUT", "remind", 2880);
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
            Order order = orderDomainService.loadOrder(cmd.getOrderId());
            releaseVehicleAssignmentIfNeeded(order, cmd.getOperateType());
            if ("cancel".equals(cmd.getOperateType())) {
                orderDomainService.cancelOrder(cmd.getOrderId(), cmd.getReason());
            } else if ("close".equals(cmd.getOperateType())) {
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

        orderLockService.executeWithLock(cmd.getOrderId(), cmd.getOperatorId(), "complete", () -> {
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
        List<Order> orderList = orderRepository.searchByAccount(
                query.getAccountId(),
                query.getOrderNo(),
                query.getOrderState() != null ? OrderState.fromValue(query.getOrderState()) : null,
                orderStateList,
                query.getHasDeliveryPerson(),
                query.getBeginTime(),
                query.getEndTime()
        );
        List<OrderListResult> results = PageUtil.convert(orderList, OrderDtoAssembler.INSTANCE::toOrderListResult);
        
        enrichOrderListResults(results);
        
        return results;
    }

    private void enrichOrderListResults(List<OrderListResult> results) {
        if (results == null || results.isEmpty()) {
            return;
        }

        // 1. 批量获取订单快照
        Map<String, OrderVehicleSnapshotPo> snapshotMap = new HashMap<>();
        for (OrderListResult result : results) {
            try {
                orderVehicleSnapshotRepository.findByOrderId(result.getOrderId())
                    .ifPresent(snapshot -> snapshotMap.put(result.getOrderId(), snapshot));
            } catch (Exception e) {
                log.warn("获取订单快照失败: orderId={}", result.getOrderId(), e);
            }
        }

        // 2. 批量获取 Variant 销售策略
        Map<String, SaleModelVariantPolicyPo> variantPolicyMap = new HashMap<>();
        for (OrderListResult result : results) {
            OrderVehicleSnapshotPo snapshot = snapshotMap.get(result.getOrderId());
            if (snapshot != null && StrUtil.isNotBlank(snapshot.getVariantCode())) {
                String key = result.getSaleModel() + "_" + snapshot.getVariantCode();
                if (!variantPolicyMap.containsKey(key)) {
                    saleModelVariantPolicyRepository
                        .findBySaleModelCodeAndVariantCode(result.getSaleModel(), snapshot.getVariantCode())
                        .ifPresent(policy -> variantPolicyMap.put(key, policy));
                }
            }
        }

        // 3. 批量获取 Option 销售策略
        Map<String, List<SaleModelOptionPolicyPo>> optionPolicyMap = new HashMap<>();
        for (OrderListResult result : results) {
            OrderVehicleSnapshotPo snapshot = snapshotMap.get(result.getOrderId());
            if (snapshot != null && StrUtil.isNotBlank(snapshot.getOptionCodes())) {
                try {
                    List<String> optionCodes = JSONUtil.toList(snapshot.getOptionCodes(), String.class);
                    if (!optionCodes.isEmpty()) {
                        List<SaleModelOptionPolicyPo> policies = optionPolicyRepository
                            .findBySaleModelCodeAndOptionCodes(result.getSaleModel(), optionCodes);
                        optionPolicyMap.put(result.getOrderId(), policies);
                    }
                } catch (Exception e) {
                    log.warn("解析 optionCodes 失败: orderId={}, optionCodes={}", 
                        result.getOrderId(), snapshot.getOptionCodes(), e);
                }
            }
        }

        // 3.4 批量获取订单金额、成功支付单与时间线最新事件时间（各一次查询，避免 N+1）
        Map<String, net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderAmountPo> amountMap = new HashMap<>();
        Map<String, Date> earnestMoneyTimeMap = new HashMap<>();
        Map<String, Date> orderStateTimeMap = new HashMap<>();
        try {
            List<String> enrichedOrderIds = results.stream()
                    .map(OrderListResult::getOrderId)
                    .filter(StrUtil::isNotBlank)
                    .distinct()
                    .collect(Collectors.toList());
            amountMap.putAll(orderAmountRepository.findPoByOrderIds(enrichedOrderIds));
            paymentRepository.findSuccessfulByOrderIds(enrichedOrderIds)
                    .stream()
                    .filter(payment -> payment.getPaymentStage() != null
                            && payment.getPaymentStage().startsWith("EARNEST"))
                    .forEach(payment -> {
                        Date payTime = toDate(payment.getPayTime());
                        if (payTime != null) {
                            earnestMoneyTimeMap.merge(payment.getOrderId(), payTime,
                                    (existing, candidate) -> existing.before(candidate) ? existing : candidate);
                        }
                    });
            orderTimelineRepository.findLatestEventTimeByOrderIds(enrichedOrderIds)
                    .forEach((orderId, eventTime) -> orderStateTimeMap.put(orderId, toDate(eventTime)));
        } catch (Exception e) {
            log.warn("批量获取订单运营字段失败", e);
        }

        // 3.5 批量获取当前有效交付预约（一次查询，避免 N+1）
        Map<String, DeliveryAppointment> appointmentMap = new HashMap<>();
        try {
            List<String> orderIds = results.stream()
                    .map(OrderListResult::getOrderId)
                    .filter(StrUtil::isNotBlank)
                    .distinct()
                    .collect(Collectors.toList());
            deliveryRepository.findBookedByOrderIds(orderIds)
                    .forEach(appointment -> appointmentMap.putIfAbsent(appointment.getOrderId(), appointment));
        } catch (Exception e) {
            log.warn("批量获取交付预约失败", e);
        }

        // 4. 组装展示信息
        for (OrderListResult result : results) {
            // 基本信息
            result.setOrderTypeName(getOrderTypeName(result.getOrderType()));
            result.setOrderSourceName(getOrderSourceName(result.getOrderSource()));
            result.setBrandName(result.getBrandCode());
            result.setOwnerRegionName(getRegionName(result.getOwnerRegionCode()));

            // 金额快照与派生时间（运营核对）
            var amountPo = amountMap.get(result.getOrderId());
            if (amountPo != null) {
                result.setEarnestMoneyAmount(amountPo.getDepositAmount());
                result.setDownPaymentAmount(amountPo.getDownPaymentAmount());
            }
            result.setEarnestMoneyTime(earnestMoneyTimeMap.get(result.getOrderId()));
            result.setOrderStateTime(orderStateTimeMap.get(result.getOrderId()));

            // 当前有效交付预约（运营交付人员分配页核对）
            DeliveryAppointment appointment = appointmentMap.get(result.getOrderId());
            if (appointment != null) {
                result.setAppointmentNo(appointment.getAppointmentNo());
                result.setAppointmentTime(appointment.getAppointmentTime());
                result.setAppointmentStatus(appointment.getAppointmentStatus() == null
                        ? null : appointment.getAppointmentStatus().getValue());
            }

            // 销售车型名称
            try {
                saleModelRepository.findBySaleModelCode(result.getSaleModel())
                    .ifPresent(po -> result.setSaleModelName(po.getModelName()));
            } catch (Exception e) {
                log.warn("获取销售车型名称失败: saleModelCode={}", result.getSaleModel(), e);
            }

            // 从快照和销售策略获取展示信息
            OrderVehicleSnapshotPo snapshot = snapshotMap.get(result.getOrderId());
            if (snapshot == null) {
                result.setSaleModelDesc("");
                result.setSaleModelImages(Collections.emptyList());
                result.setTotalPrice(BigDecimal.ZERO);
                continue;
            }

            // 设置车型、版本、配置信息
            result.setModelCode(snapshot.getModelCode());
            result.setModelName(snapshot.getModelName());
            result.setVariantCode(snapshot.getVariantCode());
            result.setVariantName(snapshot.getVariantName());
            result.setConfigurationCode(snapshot.getConfigurationCode());

            // 解析 optionCodes
            if (StrUtil.isNotBlank(snapshot.getOptionCodes())) {
                try {
                    List<String> optionCodes = JSONUtil.toList(snapshot.getOptionCodes(), String.class);
                    result.setOptionCodes(optionCodes);
                } catch (Exception e) {
                    log.warn("解析 optionCodes 失败: orderId={}, optionCodes={}",
                        result.getOrderId(), snapshot.getOptionCodes(), e);
                }
            }

            // 解析 optionBreakdown
            if (StrUtil.isNotBlank(snapshot.getOptionBreakdown())) {
                try {
                    List<VehicleInfo.OptionBreakdownItem> optionBreakdown = JSONUtil.toList(
                        snapshot.getOptionBreakdown(), VehicleInfo.OptionBreakdownItem.class);
                    result.setOptionBreakdown(optionBreakdown);
                } catch (Exception e) {
                    log.warn("解析 optionBreakdown 失败: orderId={}, optionBreakdown={}",
                        result.getOrderId(), snapshot.getOptionBreakdown(), e);
                }
            }

            // Variant 价格
            BigDecimal variantPrice = BigDecimal.ZERO;
            String variantKey = result.getSaleModel() + "_" + snapshot.getVariantCode();
            SaleModelVariantPolicyPo variantPolicy = variantPolicyMap.get(variantKey);
            if (variantPolicy != null && variantPolicy.getVariantPrice() != null) {
                variantPrice = variantPolicy.getVariantPrice();
            }

            // Option 信息
            List<SaleModelOptionPolicyPo> optionPolicies = optionPolicyMap.getOrDefault(result.getOrderId(), Collections.emptyList());
            
            // saleModelDesc = option 营销名称拼接
            String desc = optionPolicies.stream()
                .map(p -> p.getMarketingTitle() != null ? p.getMarketingTitle() : "")
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining(" "));
            result.setSaleModelDesc(desc);

            // saleModelImages = option 营销图片数组
            List<String> images = optionPolicies.stream()
                .map(SaleModelOptionPolicyPo::getMarketingImage)
                .filter(img -> img != null && !img.isEmpty())
                .collect(Collectors.toList());
            result.setSaleModelImages(images);

            // option 总价
            BigDecimal optionTotalPrice = optionPolicies.stream()
                .map(p -> p.getOptionPrice() != null ? p.getOptionPrice() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            // totalPrice = variantPrice + Σ(optionPrice)
            result.setTotalPrice(variantPrice.add(optionTotalPrice));
        }
    }

    private String getOrderTypeName(String orderType) {
        if (StrUtil.isBlank(orderType)) {
            return "";
        }
        try {
            net.hwyz.iov.cloud.otd.vso.api.enums.OrderType type = 
                    net.hwyz.iov.cloud.otd.vso.api.enums.OrderType.valueOf(orderType.toUpperCase());
            switch (type) {
                case SMALL: return "小订单";
                case FORMAL: return "正式订单";
                case MANUAL: return "手工订单";
                case REPAIR: return "补单";
                case CHANGE: return "变更单";
                case REFUND_APPLY: return "退订申请";
                case VOID: return "作废单";
                case CLOSED: return "关闭单";
                default: return "";
            }
        } catch (IllegalArgumentException e) {
            return orderType;
        }
    }

    private String getOrderSourceName(String orderSource) {
        if (StrUtil.isBlank(orderSource)) {
            return "";
        }
        try {
            net.hwyz.iov.cloud.otd.vso.api.enums.OrderSource source = 
                    net.hwyz.iov.cloud.otd.vso.api.enums.OrderSource.valueOf(orderSource.toUpperCase());
            switch (source) {
                case CAPP: return "C端自主下单";
                case SALES: return "销售代客下单";
                case STORE: return "门店代客下单";
                case OPERATION: return "运营补录";
                case IMPORT: return "外部导入";
                case ACTIVITY: return "活动订单";
                case SMALL_TO_FORMAL: return "小订单转正式";
                default: return "";
            }
        } catch (IllegalArgumentException e) {
            return orderSource;
        }
    }

    /**
     * 运营详情字段补齐（金额已在 getById 加载，这里补名称、状态时间、意向金支付时间与交付预约）
     */
    private void enrichOrderDetail(OrderDetailResult result, String orderId) {
        if (result == null || StrUtil.isBlank(orderId)) {
            return;
        }
        result.setOrderTypeName(getOrderTypeName(result.getOrderType()));
        result.setOrderSourceName(getOrderSourceName(result.getOrderSource()));
        if (StrUtil.isNotBlank(result.getBrandCode())) {
            result.setBrandName(result.getBrandCode());
        }
        if (StrUtil.isNotBlank(result.getSaleModel())) {
            try {
                saleModelRepository.findBySaleModelCode(result.getSaleModel())
                        .ifPresent(po -> result.setSaleModelName(po.getModelName()));
            } catch (Exception e) {
                log.warn("获取销售车型名称失败: saleModelCode={}", result.getSaleModel(), e);
            }
        }
        if (result.getOrderAmount() != null) {
            if (result.getOrderAmount().getDepositAmount() != null) {
                result.setEarnestMoneyAmount(result.getOrderAmount().getDepositAmount().getAmount());
            }
            if (result.getOrderAmount().getDownPaymentAmount() != null) {
                result.setDownPaymentAmount(result.getOrderAmount().getDownPaymentAmount().getAmount());
            }
        }
        try {
            List<String> orderIds = List.of(orderId);
            result.setOrderStateTime(toDate(orderTimelineRepository
                    .findLatestEventTimeByOrderIds(orderIds).get(orderId)));
            paymentRepository.findSuccessfulByOrderIds(orderIds).stream()
                    .filter(payment -> payment.getPaymentStage() != null
                            && payment.getPaymentStage().startsWith("EARNEST"))
                    .map(payment -> toDate(payment.getPayTime()))
                    .filter(Objects::nonNull)
                    .findFirst()
                    .ifPresent(result::setEarnestMoneyTime);
            deliveryRepository.findLatestByOrderId(orderId).ifPresent(appointment -> {
                result.setAppointmentNo(appointment.getAppointmentNo());
                result.setAppointmentTime(appointment.getAppointmentTime());
                result.setAppointmentStatus(appointment.getAppointmentStatus() == null
                        ? null : appointment.getAppointmentStatus().getValue());
            });
        } catch (Exception e) {
            log.warn("补齐订单运营字段失败: orderId={}", orderId, e);
        }
    }

    private Date toDate(java.time.LocalDateTime localDateTime) {
        return localDateTime == null ? null
                : Date.from(localDateTime.atZone(java.time.ZoneId.systemDefault()).toInstant());
    }

    private String getRegionName(String regionCode) {
        if (StrUtil.isBlank(regionCode)) {
            return "";
        }
        return regionCode;
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
     * <p>
     * MPT 与内部服务共用：加载金额快照并补齐运营字段（类型/来源名称、金额、状态时间、
     * 意向金支付时间、交付预约）。订单不存在统一抛 ORDER_NOT_EXIST，不得冒泡 IllegalArgumentException。
     */
    public OrderDetailResult getById(String orderId) {
        Order order = orderDomainService.loadOrder(orderId);
        try {
            loadOrderAmount(order);
        } catch (Exception e) {
            // 只读详情不得因历史金额快照缺失而失败；缺失时金额字段留空并记录待修复
            log.warn("订单[{}]金额快照缺失或读取失败，详情金额字段留空：{}", orderId, e.getMessage());
        }
        OrderDetailResult result = OrderDtoAssembler.INSTANCE.toOrderDetailResult(order);
        enrichOrderDetail(result, order.getId());
        return result;
    }

    /**
     * C 端按订单业务 ID 查询详情（用户隔离，非本人统一 ORDER_NOT_EXIST）
     */
    public OrderDetailResult getUserOrderById(String accountId, String orderId) {
        Order order = orderRepository.findByOrderIdAndAccountId(orderId, accountId)
                .orElseThrow(() -> new OrderNotExistException(orderId));
        loadOrderAmount(order);
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
    /**
     * 分配车辆（统一委托配车应用服务：VIN 占用、配车记录、状态机与时间线）
     */
    @Transactional(rollbackFor = Exception.class)
    public void assignVehicle(AssignVehicleCmd cmd) {
        vehicleAssignmentAppService.assignVehicle(cmd);
    }

    /**
     * 申请运输
     */
    @Transactional(rollbackFor = Exception.class)
    public void applyTransport(ApplyTransportCmd cmd) {
        log.info("申请运输：orderId={}", cmd.getOrderNo());
        Order order = orderDomainService.loadOrderByOrderNo(cmd.getOrderNo());
        if (order.getOrderState() != OrderState.ALLOCATION_VEHICLE) {
            throw new OrderStateNotAllowedException("车辆销售订单[" + cmd.getOrderNo() + "]当前状态["
                    + order.getOrderState() + "]不允许申请发运");
        }
        String before = String.valueOf(order.getOrderState().getValue());
        order.applyTransportVehicle(cmd.getOperatorId(), cmd.getOperatorName());
        orderRepository.save(order);
        orderTimelineAppService.record(order.getId(), OrderTimelineEventType.TRANSPORT_APPLIED,
                before, String.valueOf(order.getOrderState().getValue()),
                cmd.getOperatorId(), "mpt", "mpt", null, null,
                "发运申请人：" + cmd.getOperatorName());
    }

    /**
     * 搜索交付中心人员
     */
    public PageResult<DeliveryStaffResult> searchDeliveryStaff(String dealershipCode, Integer pageNum, Integer pageSize) {
        int page = pageNum == null || pageNum < 1 ? 1 : pageNum;
        int size = pageSize == null || pageSize < 1 ? 20 : pageSize;
        PageResult<DealershipStaffExService> staffPage = orgDealershipStaffService.searchPage(dealershipCode, page, size);
        if (staffPage == null) {
            return PageResult.empty(page, size);
        }
        List<DealershipStaffExService> staffList = staffPage.getItems() == null
                ? new ArrayList<>() : staffPage.getItems();
        // 未交付订单数一次批量统计，避免逐个交付人员 count 查询
        List<String> deliveryPersonIds = staffList.stream()
                .map(DealershipStaffExService::getUserId)
                .filter(Objects::nonNull)
                .map(String::valueOf)
                .distinct()
                .collect(Collectors.toList());
        Map<String, Integer> undeliveredCountMap = orderRepository
                .countUndeliveredByDeliveryPersonIds(deliveryPersonIds);
        List<DeliveryStaffResult> items = staffList.stream()
                .map(staff -> DeliveryStaffResult.builder()
                        .dealershipCode(staff.getDealershipCode())
                        .dealershipName(staff.getDealershipName())
                        .userId(staff.getUserId())
                        .userName(staff.getUserName())
                        .nickName(staff.getNickName())
                        .phonenumber(staff.getPhonenumber())
                        .notDeliveryOrderCount(staff.getUserId() == null ? 0
                                : undeliveredCountMap.getOrDefault(String.valueOf(staff.getUserId()), 0))
                        .build())
                .collect(Collectors.toList());
        return new PageResult<>(items, staffPage.getTotal(), staffPage.getPage(), staffPage.getSize());
    }

    /**
     * 下单前的四步校验
     */
    private void validateOrderBeforeCreate(String saleModelCode, String variantCode, List<String> optionCodes, String regionCode) {
        SaleModelPo saleModel = saleModelRepository.findBySaleModelCode(saleModelCode)
            .orElseThrow(() -> new SaleModelNotExistException("销售车型不存在: " + saleModelCode));

        if (!"active".equals(saleModel.getListingStatus())) {
            throw new SaleModelNotExistException("销售车型已下架: " + saleModelCode);
        }

        salesPolicyService.validateOptionsForSale(saleModelCode, optionCodes, regionCode);

        String configurationCode = resolveConfiguration(variantCode, optionCodes);
        if (configurationCode == null || configurationCode.isEmpty()) {
            throw new ConfigurationNotMatchedException("OptionCode 组合无法匹配到合法 Configuration");
        }

        salesPolicyService.validateConfigurationForSale(saleModelCode, configurationCode);
    }

    /**
     * 调用 MDM 服务，根据 variantCode + optionCodes 反查 configurationCode
     */
    private String resolveConfiguration(String variantCode, List<String> optionCodes) {
        try {
            ConfigurationByVariantAndOptionCodesRequest request = ConfigurationByVariantAndOptionCodesRequest.builder()
                    .variantCode(variantCode)
                    .optionCodes(optionCodes)
                    .build();
            String configCode = configurationService.resolveConfiguration(request);
            log.debug("resolveConfiguration: variantCode={}, optionCodes={} -> configurationCode={}",
                    variantCode, optionCodes, configCode);
            return configCode;
        } catch (Exception e) {
            log.error("调用 MDM resolveConfiguration 失败: variantCode={}, optionCodes={}",
                    variantCode, optionCodes, e);
            return null;
        }
    }

    // --- 以下为兼容旧接口的方法 ---

    public EarnestMoneyOrderResult earnestMoneyOrder(EarnestMoneyCmd cmd) {
        log.info("意向金下单：accountId={}, saleModel={}, modelCode={}, variantCode={}, optionCodes={}", 
                cmd.getAccountId(), cmd.getSaleModel(), cmd.getModelCode(), cmd.getVariantCode(), cmd.getOptionCodes());
        
        duplicateOrderSpecification.check(cmd.getAccountId(), null);
        
        Order order = createOrFindOrder(cmd.getAccountId(), cmd.getOrderNo());
        
        String saleModel = cmd.getSaleModel();
        String modelCode = cmd.getModelCode();
        String variantCode = cmd.getVariantCode();
        List<String> optionCodes = cmd.getOptionCodes() != null ? cmd.getOptionCodes() : new ArrayList<>();
        
        // 六步校验
        // ① SaleModel 在售校验
        SaleModelPo saleModelPo = saleModelRepository.findBySaleModelCode(saleModel)
                .orElseThrow(() -> new SaleModelNotExistException("销售车型不存在: " + saleModel));
        if (!"active".equals(saleModelPo.getListingStatus())) {
            throw new SaleModelNotExistException("销售车型已下架: " + saleModel);
        }
        
        // ② Model 销售策略校验
        salesPolicyService.validateModelForSale(saleModel, modelCode);
        
        // ③ Variant 销售策略校验
        salesPolicyService.validateVariantForSale(saleModel, variantCode);
        
        // ④ OptionCode 销售策略校验
        if (!optionCodes.isEmpty()) {
            salesPolicyService.validateOptionsForSale(saleModel, optionCodes, null);
        }
        
        // ⑤ MDM 调用 resolveConfiguration(variantCode, optionCodes)
        String configurationCode = resolveConfiguration(variantCode, optionCodes);
        if (configurationCode == null || configurationCode.isEmpty()) {
            throw new ConfigurationNotMatchedException("OptionCode 组合无法匹配到合法 Configuration");
        }
        
        // ⑥ Configuration 销售白名单校验
        salesPolicyService.validateConfigurationForSale(saleModel, configurationCode);
        
        // 获取 Variant 价格信息
        SaleModelVariantPolicyPo variantPolicy = saleModelVariantPolicyRepository
                .findBySaleModelCodeAndVariantCode(saleModel, variantCode)
                .orElse(null);
        
        BigDecimal variantPrice = BigDecimal.ZERO;
        BigDecimal earnestMoneyAmount = BigDecimal.ZERO;
        if (variantPolicy != null && "active".equals(variantPolicy.getSaleStatus())) {
            variantPrice = variantPolicy.getVariantPrice() != null ? variantPolicy.getVariantPrice() : BigDecimal.ZERO;
            earnestMoneyAmount = variantPolicy.getEarnestMoneyPrice() != null ? variantPolicy.getEarnestMoneyPrice() : BigDecimal.ZERO;
        }
        
        // 计算 Option 总价
        BigDecimal optionTotalPrice = BigDecimal.ZERO;
        for (String optionCode : optionCodes) {
            BigDecimal optionPrice = salesPolicyService.getOptionPrice(saleModel, optionCode);
            optionTotalPrice = optionTotalPrice.add(optionPrice);
        }
        
        // 保存订单配置
        order.saveConfiguration(configurationCode, cmd.getModelConfigMap());
        order.saveBrandCode(saleModelPo.getCarlineCode());
        order.saveSaleModel(saleModel);
        
        // 保存车辆配置快照
        saveOrderVehicleSnapshot(order, saleModel, saleModelPo.getCarlineCode(), modelCode, variantCode, configurationCode, optionCodes,
                variantPolicy);
        
        order.earnestMoneyOrder();
        order.saveLicenseCity(cmd.getLicenseCityCode());

        // 金额快照：只能取选中 Variant / Option 的价格，禁止使用车型最低价
        OrderAmount orderAmount = buildOrderAmountSnapshot(order, variantPrice, optionTotalPrice,
                earnestMoneyAmount, variantPolicy, true);
        order.setOrderAmount(orderAmount);

        // 购车协议确认快照（缺失必确认协议时整单回滚）
        List<OrderAgreementConfirmation> confirmations = purchaseAgreementAppService.buildConfirmations(
                order.getId(), saleModel, cmd.getAgreementConfirmations());

        orderRepository.save(order);

        saveOrderParty(order.getId(), cmd.getAccountId(), "order_user");
        purchaseAgreementAppService.saveConfirmations(confirmations);
        orderTimelineAppService.record(order.getId(), OrderTimelineEventType.ORDER_CREATED,
                null, String.valueOf(order.getOrderState().getValue()),
                cmd.getAccountId(), "order_user", "capp", order.getOrderNo(), null,
                "意向金下单，应付意向金：" + earnestMoneyAmount);

        wishlistRepository.deleteByUserId(cmd.getAccountId());
        log.info("意向金下单成功后删除心愿单：accountId={}, orderNo={}", cmd.getAccountId(), order.getOrderNo());
        
        timeoutNotifyService.createTimeoutTask(order.getId(), "SMALL_ORDER_PAY_TIMEOUT", "invalid", 
                paymentChannelConfig.getSmallOrderTimeoutMinutes());
        
        Instant expireTime = Instant.now().plusSeconds(paymentChannelConfig.getSmallOrderTimeoutMinutes() * 60);
        List<EarnestMoneyOrderResult.PaymentChannelInfo> paymentChannels = buildPaymentChannelInfoList();
        
        log.info("意向金下单完成：orderId={}, orderNo={}, configurationCode={}, earnestMoneyAmount={}", 
                order.getId(), order.getOrderNo(), configurationCode, earnestMoneyAmount);
        
        return EarnestMoneyOrderResult.builder()
                .orderNo(order.getOrderNo())
                .earnestMoneyAmount(earnestMoneyAmount)
                .paymentChannels(paymentChannels)
                .expireTime(expireTime)
                .build();
    }

    public DownPaymentOrderResult downPaymentOrder(DownPaymentCmd cmd) {
        log.info("定金下单：accountId={}, orderNo={}, saleModel={}", 
                cmd.getAccountId(), cmd.getOrderNo(), cmd.getSaleModel());

        // 旧客户端从意向金订单进入：复用 US-005 转定金用例
        if (StrUtil.isNotBlank(cmd.getOrderNo())) {
            return convertExistingOrderToDownPayment(cmd);
        }

        duplicateOrderSpecification.check(cmd.getAccountId(), null);

        String configurationCode;
        String saleModel;
        Order order = Order.fromWishlist(cmd.getAccountId(), null);
        saleModel = cmd.getSaleModel();
        configurationCode = cmd.getConfigurationCode();
        order.saveSaleModel(saleModel);
        
        if (configurationCode == null || configurationCode.isEmpty()) {
            if (cmd.getOptionCodes() != null && !cmd.getOptionCodes().isEmpty()) {
                // 调用 MDM 服务获取 configurationCode
                configurationCode = resolveConfiguration(cmd.getVariantCode(), cmd.getOptionCodes());
            }
        }
        
        if (configurationCode == null || configurationCode.isEmpty()) {
            throw new BuildConfigNotMatchedException(saleModel);
        }
        
        String carlineCode = cmd.getCarlineCode();
        if (StrUtil.isBlank(carlineCode)) {
            carlineCode = saleModelRepository.findBySaleModelCode(saleModel)
                    .map(SaleModelPo::getCarlineCode)
                    .orElse("OPENIOV");
        }
        order.saveBrandCode(carlineCode);

        order.downPaymentOrder();
        order.saveConfiguration(configurationCode, cmd.getModelConfigMap());

        // 选中 Variant 价格 / Option 总价 / 定金金额快照（禁止取车型最低价）
        SaleModelVariantPolicyPo directVariantPolicy = saleModelVariantPolicyRepository
                .findBySaleModelCodeAndVariantCode(saleModel, cmd.getVariantCode())
                .orElse(null);
        if (directVariantPolicy == null || directVariantPolicy.getDownPaymentPrice() == null) {
            throw new OrderAmountSnapshotMissingException(
                    "选中 Variant[" + cmd.getVariantCode() + "]定金价格缺失，无法下单");
        }
        BigDecimal directVariantPrice = directVariantPolicy.getVariantPrice() == null
                ? BigDecimal.ZERO : directVariantPolicy.getVariantPrice();
        List<String> directOptionCodes = cmd.getOptionCodes() == null ? new ArrayList<>() : cmd.getOptionCodes();
        BigDecimal directOptionTotal = BigDecimal.ZERO;
        for (String optionCode : directOptionCodes) {
            BigDecimal optionPrice = salesPolicyService.getOptionPrice(saleModel, optionCode);
            directOptionTotal = directOptionTotal.add(optionPrice == null ? BigDecimal.ZERO : optionPrice);
        }
        order.setOrderAmount(buildOrderAmountSnapshot(order, directVariantPrice, directOptionTotal,
                directVariantPolicy.getDownPaymentPrice(), directVariantPolicy, false));
        saveOrderVehicleSnapshot(order, saleModel, cmd.getCarlineCode(), cmd.getModelCode(), cmd.getVariantCode(),
                configurationCode, directOptionCodes, directVariantPolicy);
        List<OrderAgreementConfirmation> directConfirmations = purchaseAgreementAppService.buildConfirmations(
                order.getId(), saleModel, cmd.getAgreementConfirmations());
        
        if (StrUtil.isNotBlank(cmd.getCustomerType())) {
            order.saveCustomerType(cmd.getCustomerType());
        }
        if (StrUtil.isNotBlank(cmd.getPaymentMethod())) {
            order.savePaymentMethod(cmd.getPaymentMethod());
        }
        order.saveOrderPerson(cmd.getAccountId(), cmd.getOrderPersonType(), cmd.getOrderPersonName(),
                cmd.getOrderPersonIdType(), cmd.getOrderPersonIdNum());
        order.savePurchasePlan(cmd.getPurchasePlan());
        order.saveLicenseCity(cmd.getLicenseCityCode());
        
        if (StrUtil.isNotBlank(cmd.getOrderStoreCode())) {
            order.saveOrderStoreCode(cmd.getOrderStoreCode());
            order.saveOwnerStoreCode(cmd.getOrderStoreCode());
            if (cmd.getOrderStoreCode().length() >= 2) {
                order.saveOwnerRegionCode(cmd.getOrderStoreCode().substring(0, 2));
            }
        }
        if (StrUtil.isNotBlank(cmd.getDeliveryStoreCode())) {
            order.saveDeliveryStoreCode(cmd.getDeliveryStoreCode());
            if (cmd.getDeliveryStoreCode().length() >= 2) {
                order.saveDeliveryRegionCode(cmd.getDeliveryStoreCode().substring(0, 2));
            }
        }
        
        orderRepository.save(order);
        
        saveOrderParty(order.getId(), cmd.getAccountId(), "order_user");
        if (StrUtil.isNotBlank(cmd.getOrderPersonName()) && StrUtil.isNotBlank(cmd.getOrderPersonIdNum())) {
            saveBuyerInfo(order.getId(), cmd.getOrderPersonType(), cmd.getOrderPersonName(),
                    cmd.getOrderPersonIdType(), cmd.getOrderPersonIdNum());
        }
        
        if (StrUtil.isNotBlank(cmd.getOrderStoreCode()) || StrUtil.isNotBlank(cmd.getDeliveryStoreCode())) {
            saveOrderAssignment(order.getId(), cmd.getOrderStoreCode(), cmd.getDeliveryStoreCode());
        }
        
        purchaseAgreementAppService.saveConfirmations(directConfirmations);
        orderTimelineAppService.record(order.getId(), OrderTimelineEventType.ORDER_CREATED,
                null, String.valueOf(order.getOrderState().getValue()),
                cmd.getAccountId(), "order_user", "capp", order.getOrderNo(), null,
                "直接定金下单，应付定金：" + directVariantPolicy.getDownPaymentPrice());

        wishlistRepository.deleteByUserId(cmd.getAccountId());
        log.info("定金下单成功后删除心愿单：accountId={}, orderNo={}", cmd.getAccountId(), order.getOrderNo());

        BigDecimal downPaymentAmount = directVariantPolicy.getDownPaymentPrice();
        Instant expireTime = Instant.now().plusSeconds(paymentChannelConfig.getDownPaymentTimeoutMinutes() * 60);
        List<DownPaymentOrderResult.PaymentChannelInfo> paymentChannels = buildDownPaymentChannelInfoList();

        log.info("定金下单完成：orderId={}, orderNo={}, saleModel={}, configurationCode={}, downPaymentAmount={}", 
                order.getId(), order.getOrderNo(), saleModel, configurationCode, downPaymentAmount);

        return DownPaymentOrderResult.builder()
                .orderNo(order.getOrderNo())
                .downPaymentAmount(downPaymentAmount)
                .paymentChannels(paymentChannels)
                .expireTime(expireTime)
                .build();
    }

    /**
     * 旧客户端兼容：已有意向金订单转定金
     */
    private DownPaymentOrderResult convertExistingOrderToDownPayment(DownPaymentCmd cmd) {
        Order existing = orderRepository.findByOrderNoAndAccountId(cmd.getOrderNo(), cmd.getAccountId())
                .orElseThrow(() -> new OrderNotExistException(cmd.getOrderNo()));
        if (existing.getOrderState() != OrderState.EARNEST_MONEY_PAID) {
            throw new OrderStateNotAllowedException("车辆销售订单[" + cmd.getOrderNo() + "]当前状态["
                    + existing.getOrderState() + "]不允许定金下单");
        }
        EarnestToDownResult convertResult = earnestMoneyToDownPayment(EarnestToDownCmd.builder()
                .accountId(cmd.getAccountId())
                .orderNo(cmd.getOrderNo())
                .customerType(cmd.getCustomerType())
                .paymentMethod(cmd.getPaymentMethod())
                .orderPersonType(cmd.getOrderPersonType())
                .orderPersonName(cmd.getOrderPersonName())
                .orderPersonIdType(cmd.getOrderPersonIdType())
                .orderPersonIdNum(cmd.getOrderPersonIdNum())
                .purchasePlan(cmd.getPurchasePlan())
                .licenseCityCode(cmd.getLicenseCityCode())
                .orderStoreCode(cmd.getOrderStoreCode())
                .deliveryStoreCode(cmd.getDeliveryStoreCode())
                .build());
        BigDecimal amount = existing.getOrderAmount() == null ? BigDecimal.ZERO
                : existing.getOrderAmount().getDownPaymentAmount().getAmount();
        return DownPaymentOrderResult.builder()
                .orderNo(convertResult.getOrderNo())
                .downPaymentAmount(amount)
                .paymentChannels(buildDownPaymentChannelInfoList())
                .expireTime(Instant.now().plusSeconds(paymentChannelConfig.getDownPaymentTimeoutMinutes() * 60L))
                .build();
    }

    /**
     * 构建订单金额快照
     */
    private OrderAmount buildOrderAmountSnapshot(Order order, BigDecimal variantPrice, BigDecimal optionTotalPrice,
                                                 BigDecimal payableAmount, SaleModelVariantPolicyPo variantPolicy,
                                                 boolean earnestOrder) {
        OrderAmount amount = order.getOrderAmount() != null ? order.getOrderAmount()
                : new OrderAmount("AMT" + IdUtil.nanoId(15));
        BigDecimal vehiclePrice = variantPrice == null ? BigDecimal.ZERO : variantPrice;
        BigDecimal optionPrice = optionTotalPrice == null ? BigDecimal.ZERO : optionTotalPrice;
        amount.setGuidePrice(new Money(vehiclePrice));
        amount.setVehiclePrice(new Money(vehiclePrice));
        amount.setOptionPrice(new Money(optionPrice));
        amount.setDealPriceTotal(new Money(vehiclePrice.add(optionPrice)));
        if (earnestOrder) {
            amount.setDepositAmount(new Money(payableAmount == null ? BigDecimal.ZERO : payableAmount));
            BigDecimal downPayment = variantPolicy == null ? null : variantPolicy.getDownPaymentPrice();
            amount.setDownPaymentAmount(new Money(downPayment == null ? BigDecimal.ZERO : downPayment));
        } else {
            amount.setDepositAmount(new Money(BigDecimal.ZERO));
            amount.setDownPaymentAmount(new Money(payableAmount == null ? BigDecimal.ZERO : payableAmount));
        }
        amount.setPaidTotal(new Money(BigDecimal.ZERO));
        amount.setRefundTotal(new Money(BigDecimal.ZERO));
        amount.setUpgradedFromSmall(false);
        amount.recalculate();
        return amount;
    }

    public OrderDetailResult getUserOrder(String accountId, String orderNo) {
        Order order = findOrderById(accountId, orderNo);
        OrderDetailResult result = OrderDtoAssembler.INSTANCE.toOrderDetailResult(order);
        
        // 从订单快照获取展示信息
        enrichOrderDetailResult(order, result);
        
        if (StrUtil.isNotBlank(order.getLicenseCity())) {
            result.setLicenseCityName(getCityName(order.getLicenseCity()));
        }
        
        if (StrUtil.isNotBlank(order.getOrderStoreCode())) {
            result.setOrderStoreName(getDealershipName(order.getOrderStoreCode()));
        }
        
        if (StrUtil.isNotBlank(order.getDeliveryStoreCode())) {
            result.setDeliveryStoreName(getDealershipName(order.getDeliveryStoreCode()));
        }
        
        Optional<OrderPartyPo> buyerOpt = orderPartyRepository.findByOrderIdAndRole(order.getId(), "buyer");
        if (buyerOpt.isPresent()) {
            OrderPartyPo buyer = buyerOpt.get();
            result.setOrderPersonType(buyer.getPersonType());
            result.setOrderPersonName(buyer.getName());
            result.setOrderPersonIdType(buyer.getIdType());
            result.setOrderPersonIdNum(buyer.getIdNoEncrypted());
        }
        
        return result;
    }

    /**
     * 从订单快照和销售策略获取订单详情的展示信息
     */
    private void enrichOrderDetailResult(Order order, OrderDetailResult result) {
        if (StrUtil.isBlank(order.getSaleModel())) {
            log.warn("enrichOrderDetailResult: saleModel is blank, orderId={}", order.getId());
            return;
        }

        // 获取订单快照
        Optional<OrderVehicleSnapshotPo> snapshotOpt = orderVehicleSnapshotRepository.findByOrderId(order.getId());
        if (snapshotOpt.isEmpty()) {
            log.warn("enrichOrderDetailResult: snapshot not found, orderId={}", order.getId());
            return;
        }

        OrderVehicleSnapshotPo snapshot = snapshotOpt.get();
        log.info("enrichOrderDetailResult: snapshot found, orderId={}, modelCode={}, modelName={}, variantCode={}, variantName={}, configurationCode={}, optionCodes={}, optionBreakdown={}",
            order.getId(), snapshot.getModelCode(), snapshot.getModelName(), snapshot.getVariantCode(), snapshot.getVariantName(), snapshot.getConfigurationCode(), snapshot.getOptionCodes(), snapshot.getOptionBreakdown());

        // 设置车型、版本、配置信息
        result.setModelCode(snapshot.getModelCode());
        result.setModelName(snapshot.getModelName());
        result.setVariantCode(snapshot.getVariantCode());
        result.setVariantName(snapshot.getVariantName());
        result.setConfigurationCode(snapshot.getConfigurationCode());

        // 解析 optionCodes
        if (StrUtil.isNotBlank(snapshot.getOptionCodes())) {
            try {
                List<String> optionCodes = JSONUtil.toList(snapshot.getOptionCodes(), String.class);
                result.setOptionCodes(optionCodes);
            } catch (Exception e) {
                log.warn("解析 optionCodes 失败: orderId={}, optionCodes={}",
                    order.getId(), snapshot.getOptionCodes(), e);
            }
        }

        // 解析 optionBreakdown
        if (StrUtil.isNotBlank(snapshot.getOptionBreakdown())) {
            try {
                List<VehicleInfo.OptionBreakdownItem> optionBreakdown = JSONUtil.toList(
                    snapshot.getOptionBreakdown(), VehicleInfo.OptionBreakdownItem.class);
                result.setOptionBreakdown(optionBreakdown);
            } catch (Exception e) {
                log.warn("解析 optionBreakdown 失败: orderId={}, optionBreakdown={}",
                    order.getId(), snapshot.getOptionBreakdown(), e);
            }
        }

        // 获取 Variant 销售策略
        BigDecimal variantPrice = BigDecimal.ZERO;
        if (StrUtil.isNotBlank(snapshot.getVariantCode())) {
            saleModelVariantPolicyRepository
                .findBySaleModelCodeAndVariantCode(order.getSaleModel(), snapshot.getVariantCode())
                .ifPresent(policy -> {
                    if (policy.getVariantPrice() != null) {
                        // 这里需要设置到外部变量，使用数组包装
                    }
                });
        }

        // 获取 Option 销售策略
        List<SaleModelOptionPolicyPo> optionPolicies = Collections.emptyList();
        if (StrUtil.isNotBlank(snapshot.getOptionCodes())) {
            try {
                List<String> optionCodes = JSONUtil.toList(snapshot.getOptionCodes(), String.class);
                if (!optionCodes.isEmpty()) {
                    optionPolicies = optionPolicyRepository
                        .findBySaleModelCodeAndOptionCodes(order.getSaleModel(), optionCodes);
                }
            } catch (Exception e) {
                log.warn("解析 optionCodes 失败: orderId={}, optionCodes={}", 
                    order.getId(), snapshot.getOptionCodes(), e);
            }
        }

        // 获取 Variant 价格
        if (StrUtil.isNotBlank(snapshot.getVariantCode())) {
            saleModelVariantPolicyRepository
                .findBySaleModelCodeAndVariantCode(order.getSaleModel(), snapshot.getVariantCode())
                .ifPresent(policy -> {
                    if (policy.getVariantPrice() != null) {
                        result.setTotalPrice(policy.getVariantPrice());
                    }
                });
        }

        // saleModelDesc = option 营销名称拼接
        String desc = optionPolicies.stream()
            .map(p -> p.getMarketingTitle() != null ? p.getMarketingTitle() : "")
            .filter(s -> !s.isEmpty())
            .collect(Collectors.joining(" "));
        result.setSaleModelDesc(desc);

        // saleModelImages = option 营销图片数组
        List<String> images = optionPolicies.stream()
            .map(SaleModelOptionPolicyPo::getMarketingImage)
            .filter(img -> img != null && !img.isEmpty())
            .collect(Collectors.toList());
        result.setSaleModelImages(images);

        // option 总价
        BigDecimal optionTotalPrice = optionPolicies.stream()
            .map(p -> p.getOptionPrice() != null ? p.getOptionPrice() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // totalPrice = variantPrice + Σ(optionPrice)
        BigDecimal finalVariantPrice = variantPrice;
        if (StrUtil.isNotBlank(snapshot.getVariantCode())) {
            saleModelVariantPolicyRepository
                .findBySaleModelCodeAndVariantCode(order.getSaleModel(), snapshot.getVariantCode())
                .ifPresent(policy -> {
                    if (policy.getVariantPrice() != null) {
                        result.setTotalPrice(policy.getVariantPrice().add(optionTotalPrice));
                    }
                });
        }
    }
    
    private String getDealershipName(String dealershipCode) {
        try {
            DealershipExService dealership = orgDealershipService.getByCode(dealershipCode);
            if (dealership != null && StrUtil.isNotBlank(dealership.getName())) {
                return dealership.getName();
            }
        } catch (Exception e) {
            log.warn("获取门店名称失败: dealershipCode={}", dealershipCode, e);
        }
        return dealershipCode;
    }
    
    private String getCityName(String cityCode) {
        if (StrUtil.isBlank(cityCode)) {
            return "";
        }
        if (System.currentTimeMillis() - cityNameCacheLastRefresh < CITY_CACHE_TTL_MS) {
            String cached = cityNameCache.get(cityCode);
            if (cached != null) {
                return cached;
            }
        }
        try {
            refreshCityNameCache();
            return cityNameCache.getOrDefault(cityCode, cityCode);
        } catch (Exception e) {
            log.warn("获取城市名称失败: cityCode={}", cityCode, e);
            return cityCode;
        }
    }

    private synchronized void refreshCityNameCache() {
        if (System.currentTimeMillis() - cityNameCacheLastRefresh < CITY_CACHE_TTL_MS) {
            return;
        }
        try {
            DictionaryResponse city = dictionaryService.getDictionary("city");
            if (city != null && city.getItems() != null) {
                Map<String, String> newCache = new HashMap<>();
                for (Map<String, Object> item : city.getItems()) {
                    if (item.get("code") != null && item.get("name") != null) {
                        newCache.put(item.get("code").toString(), item.get("name").toString());
                    }
                }
                cityNameCache.clear();
                cityNameCache.putAll(newCache);
                cityNameCacheLastRefresh = System.currentTimeMillis();
            }
        } catch (Exception e) {
            log.error("刷新城市名称缓存失败", e);
        }
    }

    public void cancel(CancelCmd cmd) {
        orderLockService.executeWithLock(cmd.getOrderNo(), cmd.getAccountId(), "cancel", () -> {
            Order order = findOrderById(cmd.getAccountId(), cmd.getOrderNo());
            order.cancel();
            orderRepository.save(order);
        });
    }

    /**
     * 重新发起支付（OTD-VSO-DSN-CR-005）
     * <p>
     * 小订单支付超时（EXPIRED 960）后，按账号归属与订单锁内将订单恢复为
     * EARNEST_MONEY_UNPAID(200)，重建支付窗口与超时任务，并记 ORDER_REOPEN 时间线。
     * 恢复次数以时间线 ORDER_REOPEN 事件计数，最多 {@link #MAX_REOPEN_COUNT} 次。
     */
    @Transactional(rollbackFor = Exception.class)
    public ReopenPaymentResult reopenForPayment(ReopenPaymentCmd cmd) {
        log.info("重新发起支付：accountId={}, orderNo={}", cmd.getAccountId(), cmd.getOrderNo());
        ReopenPaymentResult result = orderLockService.executeWithLock(cmd.getOrderNo(), cmd.getAccountId(),
                "reopen", () -> doReopenForPayment(cmd));
        log.info("重新发起支付成功：orderNo={}, reopenCount={}", result.getOrderNo(), result.getReopenCount());
        return result;
    }

    private ReopenPaymentResult doReopenForPayment(ReopenPaymentCmd cmd) {
        Order order = findOrderById(cmd.getAccountId(), cmd.getOrderNo());
        if (order.getOrderType() != OrderType.SMALL) {
            throw new OrderReopenNotAllowedException(
                    "订单[" + order.getOrderNo() + "]不是小订单，不允许重新发起支付");
        }

        int windowMinutes = paymentChannelConfig.getSmallOrderTimeoutMinutes();
        int usedCount = orderTimelineRepository.countByOrderIdAndEventType(order.getId(),
                OrderTimelineEventType.ORDER_REOPEN.getCode());

        // 幂等：已在待支付状态，不重复推进状态、不追加时间线、不增加已用次数
        if (order.getOrderState() == OrderState.EARNEST_MONEY_UNPAID) {
            return buildReopenResult(order, usedCount, resolveWindowEnd(order, windowMinutes));
        }
        if (order.getOrderState() != OrderState.EXPIRED) {
            throw new OrderReopenNotAllowedException(
                    "订单[" + order.getOrderNo() + "]当前状态[" + order.getOrderState() + "]不允许重新支付");
        }

        // 已有成功支付单时拒绝恢复（防重复支付，转人工核查）
        boolean hasPaid = paymentRepository.findSuccessfulByOrderId(order.getId()).stream()
                .anyMatch(payment -> PaymentStage.EARNEST_MONEY.name().equals(payment.getPaymentStage()));
        if (hasPaid) {
            log.warn("订单已存在成功支付单，拒绝重新支付并转人工核查：orderNo={}", order.getOrderNo());
            throw new OrderReopenNotAllowedException(
                    "订单[" + order.getOrderNo() + "]已存在成功支付单，不允许重新发起支付");
        }

        if (usedCount >= MAX_REOPEN_COUNT) {
            throw new OrderReopenLimitExceededException(
                    "订单[" + order.getOrderNo() + "]重新支付次数已用尽，请重新下单");
        }

        // 强制关闭该阶段非终态支付单，避免复用死单
        closeNonTerminalPayments(order.getId(), PaymentStage.EARNEST_MONEY);

        order.reopenForPayment();
        orderRepository.save(order);

        // 重建支付窗口与超时任务（同订单同类型仅保留一个待触发任务）
        timeoutNotifyService.cancelByOrderIdAndType(order.getId(), "SMALL_ORDER_PAY_TIMEOUT");
        timeoutNotifyService.createTimeoutTask(order.getId(), "SMALL_ORDER_PAY_TIMEOUT", "invalid", windowMinutes);

        int reopenCount = usedCount + 1;
        Instant expireTime = Instant.now().plusSeconds(windowMinutes * 60L);
        orderTimelineAppService.record(order.getId(), OrderTimelineEventType.ORDER_REOPEN,
                String.valueOf(OrderState.EXPIRED.getValue()),
                String.valueOf(OrderState.EARNEST_MONEY_UNPAID.getValue()),
                cmd.getAccountId(), "order_user", "capp", order.getOrderNo(), null,
                "第 " + reopenCount + " 次重新发起支付，窗口截止：" + expireTime);

        return buildReopenResult(order, reopenCount, expireTime);
    }

    /**
     * 关闭指定订单指定支付阶段的非终态支付单
     */
    private void closeNonTerminalPayments(String orderId, PaymentStage stage) {
        for (Payment payment : paymentRepository.findByOrderId(orderId)) {
            if (!stage.name().equals(payment.getPaymentStage())) {
                continue;
            }
            if (payment.forceClose("订单重新发起支付，关闭原支付单")) {
                paymentRepository.save(payment);
                log.info("重新发起支付关闭非终态支付单：paymentNo={}, status={}",
                        payment.getPaymentNo(), payment.getPaymentStatus());
            }
        }
    }

    /**
     * 幂等分支的窗口截止时间：优先取现有活动支付单的截止时间
     */
    private Instant resolveWindowEnd(Order order, int windowMinutes) {
        return paymentRepository.findActiveByOrderIdAndStage(order.getId(), PaymentStage.EARNEST_MONEY.name())
                .filter(payment -> payment.getExpireTime() != null)
                .map(payment -> payment.getExpireTime().atZone(ZoneId.systemDefault()).toInstant())
                .orElseGet(() -> {
                    if (order.getOrderStateTime() != null) {
                        return order.getOrderStateTime().toInstant().plusSeconds(windowMinutes * 60L);
                    }
                    return Instant.now().plusSeconds(windowMinutes * 60L);
                });
    }

    private ReopenPaymentResult buildReopenResult(Order order, int reopenCount, Instant expireTime) {
        return ReopenPaymentResult.builder()
                .orderNo(order.getOrderNo())
                .orderState(order.getOrderState().getValue())
                .reopenCount(reopenCount)
                .expireTime(expireTime)
                .build();
    }

    /**
     * 申请退款（委托退款应用服务；多支付分摊、失败重试与回调闭环）
     */
    public RefundResult requestRefund(RequestRefundCmd cmd) {
        return refundAppService.apply(cmd.getAccountId(), cmd.getOrderNo());
    }

    /**
     * 意向金转定金（支持差额支付）
     *
     * 流程：
     * 1. 校验订单状态
     * 2. 计算差额 = 定金金额 - 意向金金额
     * 3. 如果差额 > 0：创建差额支付任务，返回支付信息
     * 4. 如果差额 <= 0：直接完成状态转换
     */
    public EarnestToDownResult earnestMoneyToDownPayment(EarnestToDownCmd cmd) {
        log.info("意向金转定金：accountId={}, orderNo={}", cmd.getAccountId(), cmd.getOrderNo());

        if (StrUtil.isNotBlank(cmd.getCustomerType()) && !CustomerType.isValid(cmd.getCustomerType())) {
            log.warn("客户类型不合法：customerType={}", cmd.getCustomerType());
            throw new IllegalArgumentException("客户类型不合法，仅支持：personal");
        }
        if (StrUtil.isNotBlank(cmd.getPaymentMethod()) && !PaymentMethod.isValid(cmd.getPaymentMethod())) {
            log.warn("支付方式不合法：paymentMethod={}", cmd.getPaymentMethod());
            throw new IllegalArgumentException("支付方式不合法，仅支持：full_payment、loan");
        }

        return orderLockService.executeWithLock(cmd.getOrderNo(), cmd.getAccountId(), "convert", () -> {
            Order order = findOrderById(cmd.getAccountId(), cmd.getOrderNo());

            // 幂等性检查：如果已经是正式订单且状态为DOWN_PAYMENT_PAID，直接返回成功
            if (order.getOrderType() == OrderType.FORMAL && order.getOrderState() == OrderState.DOWN_PAYMENT_PAID) {
                log.info("订单已转为正式订单，幂等返回：orderNo={}", cmd.getOrderNo());
                return EarnestToDownResult.builder()
                    .orderNo(order.getOrderNo())
                    .orderType(order.getOrderType())
                    .orderState(order.getOrderState())
                    .build();
            }

            // 校验订单状态
            if (order.getOrderState() != OrderState.EARNEST_MONEY_PAID) {
                log.warn("订单状态不允许转定金：orderNo={}, orderState={}",
                    order.getOrderNo(), order.getOrderState());
                throw new OrderStateNotAllowedException("车辆销售订单[" + order.getOrderNo() + "]当前状态[" + order.getOrderState() + "]不允许转定金");
            }

            // 保存补充信息
            saveSupplementaryInfo(order, cmd);

            // 计算差额
            if (order.getOrderAmount() == null) {
                log.error("订单金额信息缺失，无法执行意向金转定金：orderNo={}, orderId={}", cmd.getOrderNo(), order.getId());
                throw new OrderStateNotAllowedException("车辆销售订单[" + order.getOrderNo() + "]金额信息缺失，请联系管理员");
            }
            Money difference = order.getOrderAmount().calculateEarnestToDownDifference();
            log.info("意向金转定金差额计算：orderNo={}, difference={}", cmd.getOrderNo(), difference);

            if (difference.isPositive()) {
                // 差额 > 0：存在未完成补款任务时返回已有任务（幂等）
                SupplementaryPaymentPo supplementaryPayment = supplementaryPaymentRepository
                    .findPendingByOrderIdAndScene(order.getId(),
                            SupplementaryPaymentScene.EARNEST_TO_DOWN.getValue())
                    .orElseGet(() -> createEarnestToDownSupplementaryPayment(order.getId(), difference));

                if (order.getOrderAmount() != null && order.getOrderAmount().getPaidTotal()
                        .isGreaterThan(order.getOrderAmount().getDownPaymentAmount())) {
                    order.getOrderAmount().setUpgradedFromSmall(true);
                    orderRepository.save(order);
                }
                orderTimelineAppService.record(order.getId(), OrderTimelineEventType.EARNEST_TO_DOWN_REQUESTED,
                        String.valueOf(order.getOrderState().getValue()),
                        String.valueOf(order.getOrderState().getValue()),
                        cmd.getAccountId(), "order_user", "capp", supplementaryPayment.getSupplementaryNo(), null,
                        "差额补款：" + difference.getAmount());

                log.info("意向金转定金差额支付任务已创建：orderNo={}, difference={}, supplementaryNo={}",
                    cmd.getOrderNo(), difference, supplementaryPayment.getSupplementaryNo());

                return EarnestToDownResult.builder()
                    .orderNo(order.getOrderNo())
                    .orderType(order.getOrderType())
                    .orderState(order.getOrderState())
                    .supplementaryPayment(SupplementaryPaymentInfo.builder()
                        .supplementaryNo(supplementaryPayment.getSupplementaryNo())
                        .amount(new Money(supplementaryPayment.getSupplementaryAmount()))
                        .expireTime(supplementaryPayment.getExpireTime())
                        .build())
                    .build();
            } else {
                // 差额 <= 0：直接完成状态转换
                OrderState beforeState = order.getOrderState();
                order.earnestMoneyToDownPayment();
                orderRepository.save(order);

                // 保存买家信息和订单分配
                saveRelatedInfo(order, cmd);

                orderTimelineAppService.record(order.getId(), OrderTimelineEventType.EARNEST_TO_DOWN_COMPLETED,
                    String.valueOf(beforeState.getValue()), String.valueOf(order.getOrderState().getValue()),
                    cmd.getAccountId(), "order_user", "capp", null, null,
                    "意向金订单转定金订单（差额<=0，直接转换）");

                // 记录时间线
                saveOrderTimeline(order.getId(), "EARNEST_TO_DOWN_PAYMENT", "意向金转定金",
                    beforeState.getValue().toString(), order.getOrderState().getValue().toString(),
                    cmd.getAccountId(), "order_user", "capp",
                    null, "success", null,
                    "意向金订单转定金订单（差额<=0，直接转换）");

                log.info("意向金转定金完成（直接转换）：orderId={}, orderNo={}, orderType={}",
                    order.getId(), order.getOrderNo(), order.getOrderType());

                return EarnestToDownResult.builder()
                    .orderNo(order.getOrderNo())
                    .orderType(order.getOrderType())
                    .orderState(order.getOrderState())
                    .build();
            }
        });
    }

    /**
     * 保存补充信息
     */
    private void saveSupplementaryInfo(Order order, EarnestToDownCmd cmd) {
        if (StrUtil.isNotBlank(cmd.getCustomerType())) {
            order.saveCustomerType(cmd.getCustomerType());
        }
        if (StrUtil.isNotBlank(cmd.getPaymentMethod())) {
            order.savePaymentMethod(cmd.getPaymentMethod());
        }
        order.saveOrderPerson(cmd.getAccountId(), cmd.getOrderPersonType(), cmd.getOrderPersonName(),
            cmd.getOrderPersonIdType(), cmd.getOrderPersonIdNum());
        order.savePurchasePlan(cmd.getPurchasePlan());
        if (StrUtil.isNotBlank(cmd.getLicenseCityCode())) {
            order.saveLicenseCity(cmd.getLicenseCityCode());
        }
        if (StrUtil.isNotBlank(cmd.getOrderStoreCode())) {
            order.saveOrderStoreCode(cmd.getOrderStoreCode());
            order.saveOwnerStoreCode(cmd.getOrderStoreCode());
            if (cmd.getOrderStoreCode().length() >= 2) {
                order.saveOwnerRegionCode(cmd.getOrderStoreCode().substring(0, 2));
            }
        }
        if (StrUtil.isNotBlank(cmd.getDeliveryStoreCode())) {
            order.saveDeliveryStoreCode(cmd.getDeliveryStoreCode());
            if (cmd.getDeliveryStoreCode().length() >= 2) {
                order.saveDeliveryRegionCode(cmd.getDeliveryStoreCode().substring(0, 2));
            }
        }
        orderRepository.save(order);
    }

    /**
     * 保存关联信息（买家信息和订单分配）
     */
    private void saveRelatedInfo(Order order, EarnestToDownCmd cmd) {
        if (StrUtil.isNotBlank(cmd.getOrderPersonName()) && StrUtil.isNotBlank(cmd.getOrderPersonIdNum())) {
            saveBuyerInfo(order.getId(), cmd.getOrderPersonType(), cmd.getOrderPersonName(),
                cmd.getOrderPersonIdType(), cmd.getOrderPersonIdNum());
        }
        if (StrUtil.isNotBlank(cmd.getOrderStoreCode()) || StrUtil.isNotBlank(cmd.getDeliveryStoreCode())) {
            saveOrderAssignment(order.getId(), cmd.getOrderStoreCode(), cmd.getDeliveryStoreCode());
        }
    }

    /**
     * 创建意向金转定金差额支付任务
     */
    private SupplementaryPaymentPo createEarnestToDownSupplementaryPayment(String orderId, Money amount) {
        String supplementaryNo = "SUP" + System.currentTimeMillis() + String.format("%04d", new java.util.Random().nextInt(10000));
        LocalDateTime expireTime = LocalDateTime.now().plusMinutes(30);

        SupplementaryPaymentPo supplementaryPayment = SupplementaryPaymentPo.builder()
            .supplementaryNo(supplementaryNo)
            .orderId(orderId)
            .supplementaryAmount(amount.getAmount())
            .currency(amount.getCurrency())
            .supplementaryStatus(SupplementaryPaymentStatus.PENDING.getValue())
            .supplementaryScene(SupplementaryPaymentScene.EARNEST_TO_DOWN.getValue())
            .configVersionNo(0)
            .expireTime(expireTime)
            .build();

        supplementaryPaymentRepository.save(supplementaryPayment);
        return supplementaryPayment;
    }

    public void lock(LockCmd cmd) {
        Order order = findOrderById(cmd.getAccountId(), cmd.getOrderNo());
        orderLockService.executeWithLock(order.getId(), cmd.getAccountId(), "lock", () -> {
            Order current = findOrderById(cmd.getAccountId(), cmd.getOrderNo());
            if (current.getOrderState() == OrderState.ARRANGE_PRODUCTION) {
                return;
            }
            String before = String.valueOf(current.getOrderState().getValue());
            current.lock();
            orderRepository.save(current);
            orderTimelineAppService.record(current.getId(), OrderTimelineEventType.ORDER_LOCKED,
                    before, String.valueOf(current.getOrderState().getValue()),
                    cmd.getAccountId(), "order_user", "capp", null, null, "用户主动锁单");
        });
    }

    /**
     * 修改订单配置（改配）
     * 支持价格重算与差额补/退
     */
    @Transactional(rollbackFor = Exception.class)
    public void modifyConfig(ModifyOrderConfigCmd cmd) {
        log.info("修改订单配置：accountId={}, orderNo={}, optionCodes={}",
                cmd.getAccountId(), cmd.getOrderNo(), cmd.getOptionCodes());

        orderLockService.executeWithLock(cmd.getOrderNo(), cmd.getAccountId(), "modifyConfig", () -> {
            // 1. 查找订单（本人订单，非本人统一 ORDER_NOT_EXIST）
            Order order = findOrderById(cmd.getAccountId(), cmd.getOrderNo());
            doModifyConfig(order, cmd.getVariantCode(), cmd.getOptionCodes(),
                    cmd.getAccountId(), "order_user", "capp", null);
        });
    }

    /**
     * MPT 运营改配提交（US-008 补充）
     * <p>
     * 与 C 端改配复用同一套配置解析、价格差额补/退与快照逻辑；
     * 差异仅为：不做 C 端账号归属校验、操作人取安全上下文、时间线记录 operateSource=mpt。
     *
     * @param cmd 运营改配命令
     */
    @Transactional(rollbackFor = Exception.class)
    public void modifyConfigByOperator(ModifyOrderConfigByOperatorCmd cmd) {
        log.info("MPT 修改订单配置：operatorId={}, orderId={}, optionCodes={}",
                cmd.getOperatorId(), cmd.getOrderId(), cmd.getOptionCodes());
        Order order = orderRepository.findByOrderId(cmd.getOrderId())
                .orElseThrow(() -> new OrderNotExistException(cmd.getOrderId()));
        orderLockService.executeWithLock(order.getOrderNo(), cmd.getOperatorId(), "modifyConfig", () -> {
            // 锁内重新加载，与 C 端改配保持一致
            Order current = orderRepository.findByOrderId(cmd.getOrderId())
                    .orElseThrow(() -> new OrderNotExistException(cmd.getOrderId()));
            doModifyConfig(current, cmd.getVariantCode(), cmd.getOptionCodes(),
                    cmd.getOperatorId(), "mpt_operator", "mpt", cmd.getReason());
        });
    }

    /**
     * 改配共用实现（C 端与 MPT 复用，禁止在 Controller 各自拼装）
     */
    private void doModifyConfig(Order order, String variantCode, List<String> optionCodes,
                                String operatorId, String operatorRole, String operateSource, String reason) {
            // 1. 金额快照（C 端入口已加载，MPT 入口在此补齐，缺失时明确报错）
            if (order.getOrderAmount() == null) {
                loadOrderAmount(order);
            }

            // 2. 校验订单状态
            validateOrderStateForModifyConfig(order);

            // 3. 获取新的 buildConfigCode（通过 variantCode + optionCodes）
            String newBuildConfigCode = resolveConfiguration(variantCode, optionCodes);
            if (newBuildConfigCode == null || newBuildConfigCode.isEmpty()) {
                throw new BuildConfigNotMatchedException(order.getSaleModel());
            }

            // 4. 获取 buildConfig 详情
            VmdBuildConfigResponse buildConfig = vmdVehicleModelConfigService.getBuildConfigByCode(newBuildConfigCode);
            if (buildConfig == null || buildConfig.getBrandCode() == null) {
                throw new BrandCodeNotExistException(newBuildConfigCode);
            }

            // 5. 保存旧配置信息用于价格比较
            String oldConfigurationCode = order.getConfigurationCode();
            Money oldVehiclePrice = order.getCurrentVehiclePrice();
            Money oldOptionPrice = order.getCurrentOptionPrice();
            Money oldTotalPrice = oldVehiclePrice.add(oldOptionPrice);

            // 6. 获取新配置的价格（通过销售策略表）
            BigDecimal variantPrice = BigDecimal.ZERO;
            if (StrUtil.isNotBlank(variantCode)) {
                variantPrice = saleModelVariantPolicyRepository
                    .findBySaleModelCodeAndVariantCode(order.getSaleModel(), variantCode)
                    .map(policy -> policy.getVariantPrice() != null ? policy.getVariantPrice() : BigDecimal.ZERO)
                    .orElse(BigDecimal.ZERO);
            }

            BigDecimal optionTotalPrice = BigDecimal.ZERO;
            if (optionCodes != null && !optionCodes.isEmpty()) {
                List<SaleModelOptionPolicyPo> optionPolicies = optionPolicyRepository
                    .findBySaleModelCodeAndOptionCodes(order.getSaleModel(), optionCodes);
                optionTotalPrice = optionPolicies.stream()
                    .map(p -> p.getOptionPrice() != null ? p.getOptionPrice() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            }

            BigDecimal newTotalPriceAmount = variantPrice.add(optionTotalPrice);
            Money newTotalPrice = Money.of(newTotalPriceAmount, "CNY");

            // 7. 计算价格差额
            Money priceDifference = newTotalPrice.subtract(oldTotalPrice);

            // 8. 更新订单配置
            order.saveConfiguration(newBuildConfigCode, null);
            order.saveBrandCode(buildConfig.getBrandCode());

            // 9. 保存新的车型快照
            String saleModelName = saleModelRepository.findBySaleModelCode(order.getSaleModel())
                    .map(SaleModelPo::getModelName)
                    .orElse("");
            saveOrderVehicleSnapshotWithVersionIncrement(order, order.getSaleModel(), saleModelName, variantCode, buildConfig);

            // 10. 更新订单金额（将新总价分配到车辆价格，选装价格设为0）
            order.updateAmountForConfigChange(newTotalPrice, Money.ZERO_CNY);

            // 11. 持久化订单
            orderRepository.save(order);

            // 12. 处理价格差额
            String supplementaryNo = null;
            String refundTaskNo = null;

            if (priceDifference.isGreaterThan(Money.ZERO_CNY)) {
                // 差额 > 0：创建补款任务
                supplementaryNo = createSupplementaryPaymentTask(order, priceDifference);
                log.info("改配成功，创建补款任务: orderNo={}, amount={}", order.getOrderNo(), priceDifference);
            } else if (priceDifference.isLessThan(Money.ZERO_CNY)) {
                // 差额 < 0：创建退款任务
                refundTaskNo = createConfigChangeRefundTask(order, priceDifference.abs());
                log.info("改配成功，创建退款任务: orderNo={}, amount={}", order.getOrderNo(), priceDifference.abs());
            } else {
                log.info("改配成功，无价格差额: orderNo={}", order.getOrderNo());
            }

            // 13. 发布领域事件
            eventPublisher.publishEvent(ConfigChangePriceDiffEvent.builder()
                    .orderId(order.getId())
                    .orderNo(order.getOrderNo())
                    .accountId(resolveOrderOwnerAccountId(order, operatorId))
                    .priceDifference(priceDifference.getAmount())
                    .newConfigVersionNo(order.getCurrentVersionNo())
                    .supplementaryNo(supplementaryNo)
                    .refundTaskNo(refundTaskNo)
                    .build());

            // 14. 记录操作时间线（操作人/来源按调用方语义记录，MPT 不得写死 capp）
            saveOrderTimeline(order.getId(), "MODIFY_CONFIG", "修改订单配置",
                    oldConfigurationCode, newBuildConfigCode,
                    operatorId, operatorRole, operateSource,
                    null, "success", null,
                    String.format("旧配置: %s, 新配置: %s, 价格差额: %s%s",
                            oldConfigurationCode, newBuildConfigCode, priceDifference,
                            StrUtil.isBlank(reason) ? "" : "，原因: " + reason));
    }

    /**
     * 改配领域事件使用的下单账号：优先取订单 order_user，取不到时回退操作人
     */
    private String resolveOrderOwnerAccountId(Order order, String operatorId) {
        return orderPartyRepository.findByOrderIdAndRole(order.getId(), "order_user")
                .map(net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderPartyPo::getUserId)
                .orElse(operatorId);
    }

    private void validateOrderStateForModifyConfig(Order order) {
        OrderState state = order.getOrderState();
        if (state == null || !state.isConfigModifiable()) {
            throw new OrderStateNotAllowedException("车辆销售订单[" + order.getOrderNo() + "]当前状态[" + state + "]不允许修改配置");
        }
    }

    private String getBuildConfigCodeFromFeatureConfig(Map<String, String> featureConfig) {
        if (featureConfig == null || featureConfig.isEmpty()) {
            return null;
        }
        Map<String, String> config = new HashMap<>(featureConfig);
        config.remove("BASE_MODEL");
        return vmdVehicleModelConfigService.getVehicleBuildConfigCode(config);
    }

    private void saveOrderVehicleSnapshotWithVersionIncrement(Order order, String saleModelCode, 
            String saleModelName, String variantCode, VmdBuildConfigResponse buildConfig) {
        if (StrUtil.isBlank(saleModelCode)) {
            log.warn("订单缺少saleModelCode，无法生成车型快照：orderNo={}", order.getOrderNo());
            return;
        }
        
        if (buildConfig == null || StrUtil.isBlank(buildConfig.getCode())) {
            log.warn("订单缺少buildConfig，无法生成车型快照：orderNo={}", order.getOrderNo());
            return;
        }
        
        // 获取modelName从vso_sale_model_model_policy表
        String modelName = saleModelModelPolicyRepository
                .findBySaleModelCodeAndModelCode(saleModelCode, buildConfig.getModelCode())
                .map(p -> p.getMarketingName() != null ? p.getMarketingName() : "")
                .orElse("");
        
        // 获取版本营销名称和价格
        String variantMarketingName = "";
        BigDecimal variantPrice = BigDecimal.ZERO;
        if (StrUtil.isNotBlank(variantCode)) {
            SaleModelVariantPolicyPo variantPolicy = saleModelVariantPolicyRepository
                    .findBySaleModelCodeAndVariantCode(saleModelCode, variantCode)
                    .orElse(null);
            if (variantPolicy != null) {
                variantMarketingName = variantPolicy.getMarketingName() != null ? variantPolicy.getMarketingName() : "";
                variantPrice = variantPolicy.getVariantPrice() != null ? variantPolicy.getVariantPrice() : BigDecimal.ZERO;
            }
        }
        
        // 获取配置名称从ConfigurationService
        String configurationName = "";
        try {
            ConfigurationResponse configResp = configurationService.getByCode(buildConfig.getCode());
            if (configResp != null) {
                configurationName = configResp.getName() != null ? configResp.getName() : "";
            }
        } catch (Exception e) {
            log.warn("获取配置名称失败: configurationCode={}", buildConfig.getCode(), e);
        }
        
        orderVehicleSnapshotRepository.logicalDeleteByOrderId(order.getId());
        
        Integer currentVersion = orderVehicleSnapshotRepository.findMaxVersionByOrderId(order.getId());
        Integer newVersion = currentVersion + 1;
        
        OrderVehicleSnapshotPo snapshotPo = new OrderVehicleSnapshotPo();
        snapshotPo.setSnapshotId(IdUtil.nanoId(15));
        snapshotPo.setOrderId(order.getId());
        snapshotPo.setSaleModelCode(saleModelCode);
        snapshotPo.setSaleModelName(saleModelName);
        snapshotPo.setCarlineCode(buildConfig.getBrandCode());
        snapshotPo.setModelCode(buildConfig.getModelCode());
        snapshotPo.setModelName(modelName);
        snapshotPo.setVariantCode(variantCode);
        snapshotPo.setVariantName(variantMarketingName);
        snapshotPo.setConfigurationCode(buildConfig.getCode());
        snapshotPo.setConfigurationName(configurationName);
        snapshotPo.setVariantPrice(variantPrice);
        snapshotPo.setSnapshotVersion(newVersion);
        
        orderVehicleSnapshotRepository.save(snapshotPo);
        log.info("保存订单车型快照(新版本)：orderId={}, version={}, configurationCode={}, variantPrice={}", 
                order.getId(), newVersion, buildConfig.getCode(), variantPrice);
    }

    public void prepareTransport(PrepareTransportCmd cmd) {
        Order order = findOrderByOrderNo(cmd.getOrderNo());
        if (order.getOrderState() != OrderState.APPLY_TRANSPORT) {
            throw new OrderStateNotAllowedException("车辆销售订单[" + cmd.getOrderNo() + "]当前状态["
                    + order.getOrderState() + "]不允许流转为待运输");
        }
        String before = String.valueOf(order.getOrderState().getValue());
        order.prepareTransport();
        orderRepository.save(order);
        orderTimelineAppService.record(order.getId(), OrderTimelineEventType.PREPARE_TRANSPORT,
                before, String.valueOf(order.getOrderState().getValue()),
                "system", "service", "logistics_service", null, null, "物流服务通知待运输");
        timeoutNotifyService.createTimeoutTask(order.getId(), "PREPARE_TRANSPORT_TIMEOUT", "remind", 1440);
    }

    public void transporting(TransportingCmd cmd) {
        Order order = findOrderByOrderNo(cmd.getOrderNo());
        if (order.getOrderState() != OrderState.PREPARE_TRANSPORT) {
            throw new OrderStateNotAllowedException("车辆销售订单[" + cmd.getOrderNo() + "]当前状态["
                    + order.getOrderState() + "]不允许流转为运输中");
        }
        String before = String.valueOf(order.getOrderState().getValue());
        timeoutNotifyService.cancelByOrderIdAndType(order.getId(), "PREPARE_TRANSPORT_TIMEOUT");
        order.transporting();
        orderRepository.save(order);
        orderTimelineAppService.record(order.getId(), OrderTimelineEventType.TRANSPORTING,
                before, String.valueOf(order.getOrderState().getValue()),
                "system", "service", "logistics_service", null, null, "物流服务通知已发车");
        timeoutNotifyService.createTimeoutTask(order.getId(), "TRANSPORTING_TIMEOUT", "remind", 2880);
    }

    public void prepareDelivery(PrepareDeliveryCmd cmd) {
        Order order = findOrderByOrderNo(cmd.getOrderNo());
        if (order.getOrderState() != OrderState.TRANSPORTING) {
            throw new OrderStateNotAllowedException("车辆销售订单[" + cmd.getOrderNo() + "]当前状态["
                    + order.getOrderState() + "]不允许流转为待交付");
        }
        String before = String.valueOf(order.getOrderState().getValue());
        timeoutNotifyService.cancelByOrderIdAndType(order.getId(), "TRANSPORTING_TIMEOUT");
        order.prepareDelivery();
        orderRepository.save(order);
        orderTimelineAppService.record(order.getId(), OrderTimelineEventType.PREPARE_DELIVERY,
                before, String.valueOf(order.getOrderState().getValue()),
                "system", "service", "logistics_service", null, null, "车辆到达交付中心");
        timeoutNotifyService.createTimeoutTask(order.getId(), "PREPARE_DELIVER_TIMEOUT", "remind", 1440);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delivered(DeliveredCmd cmd) {
        Order order = findOrderByOrderNo(cmd.getOrderNo());
        if (order.getOrderState() != OrderState.PREPARE_DELIVER) {
            if (order.getOrderState() == OrderState.DELIVERED) {
                log.info("订单已交付，交付回调幂等：orderNo={}", cmd.getOrderNo());
                return;
            }
            throw new OrderStateNotAllowedException("车辆销售订单[" + cmd.getOrderNo() + "]当前状态["
                    + order.getOrderState() + "]不允许交付");
        }
        String before = String.valueOf(order.getOrderState().getValue());
        timeoutNotifyService.cancelByOrderIdAndType(order.getId(), "PREPARE_DELIVER_TIMEOUT");
        order.delivered();
        orderRepository.save(order);
        deliveryAppService.completeDelivery(order, "delivery_service");
        orderTimelineAppService.record(order.getId(), OrderTimelineEventType.ORDER_DELIVERED,
                before, String.valueOf(order.getOrderState().getValue()),
                "system", "service", "delivery_service", null, null, "外部交付服务确认已交付");
        timeoutNotifyService.createTimeoutTask(order.getId(), "DELIVERED_TIMEOUT", "remind", 4320);
    }

    public void activate(ActivateCmd cmd) {
        Order order = findOrderByOrderNo(cmd.getOrderNo());
        if (order.getOrderState() != OrderState.DELIVERED && order.getOrderState() != OrderState.ACTIVATED) {
            throw new OrderStateNotAllowedException("车辆销售订单[" + cmd.getOrderNo() + "]当前状态["
                    + order.getOrderState() + "]不允许激活，仅 DELIVERED 可激活");
        }
        if (order.getOrderState() == OrderState.ACTIVATED) {
            log.info("订单已激活，激活回调幂等：orderNo={}", cmd.getOrderNo());
            return;
        }
        String before = String.valueOf(order.getOrderState().getValue());
        timeoutNotifyService.cancelByOrderIdAndType(order.getId(), "DELIVERED_TIMEOUT");
        order.activate();
        orderRepository.save(order);
        orderTimelineAppService.record(order.getId(), OrderTimelineEventType.ORDER_ACTIVATED,
                before, String.valueOf(order.getOrderState().getValue()),
                "system", "service", "vehicle_service", null, null, "外部车辆服务确认已激活");
    }

    /**
     * 查询本人订单时间线
     */
    public List<OrderTimelineResult> getOrderTimeline(String accountId, String orderNo) {
        Order order = findOrderById(accountId, orderNo);
        return orderTimelineAppService.timelineOf(order.getId());
    }

    /**
     * 运营（MPT）按业务订单 ID 查询订单时间线（只读，不做账号隔离）
     */
    public List<OrderTimelineResult> getTimelineByOrderId(String orderId) {
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new OrderNotExistException(orderId));
        return orderTimelineAppService.timelineOf(order.getId());
    }

    private Order findOrderById(String accountId, String orderNo) {
        Optional<Order> orderOpt = orderRepository.findByOrderNoAndAccountId(orderNo, accountId);
        if (orderOpt.isEmpty()) {
            throw new OrderNotExistException(orderNo);
        }
        Order order = orderOpt.get();
        loadOrderAmount(order);
        return order;
    }

    private Order findOrderByOrderNo(String orderNo) {
        Optional<Order> orderOpt = orderRepository.findByOrderNo(orderNo);
        if (orderOpt.isEmpty()) {
            throw new OrderNotExistException(orderNo);
        }
        Order order = orderOpt.get();
        loadOrderAmount(order);
        return order;
    }

    private void loadOrderAmount(Order order) {
        if (order.getId() == null) {
            log.warn("订单ID为空，无法加载金额信息：orderNo={}", order.getOrderNo());
            return;
        }
        orderAmountRepository.findDomainByOrderId(order.getId())
                .ifPresentOrElse(
                        order::setOrderAmount,
                        () -> log.warn("订单金额记录不存在：orderId={}, orderNo={}", order.getId(), order.getOrderNo())
                );
    }

    private Order createOrFindOrder(String accountId, String orderNo) {
        Optional<Order> orderOpt = orderRepository.findByOrderNoAndAccountId(orderNo, accountId);
        if (orderOpt.isPresent()) {
            return orderOpt.get();
        }
        return Order.fromWishlist(accountId, null);
    }

    private void saveOrderParty(String orderId, String userId, String partyRole) {
        OrderPartyPo orderPartyPo = new OrderPartyPo();
        orderPartyPo.setPartyId(IdUtil.nanoId(15));
        orderPartyPo.setOrderId(orderId);
        orderPartyPo.setPartyRole(partyRole);
        orderPartyPo.setUserId(userId);
        orderPartyPo.setAuthorizedFlag(0);
        orderPartyRepository.save(orderPartyPo);
        log.info("保存订单客户信息：orderId={}, userId={}, partyRole={}", orderId, userId, partyRole);
    }

    private void saveBuyerInfo(String orderId, Integer personType, String buyerName,
                                Integer idType, String buyerIdNo) {
        OrderPartyPo buyerPo = new OrderPartyPo();
        buyerPo.setPartyId(IdUtil.nanoId(15));
        buyerPo.setOrderId(orderId);
        buyerPo.setPartyRole("buyer");
        buyerPo.setPersonType(personType);
        buyerPo.setName(buyerName);
        buyerPo.setIdType(idType);
        buyerPo.setIdNoEncrypted(buyerIdNo);
        buyerPo.setAuthorizedFlag(0);
        orderPartyRepository.save(buyerPo);
        log.info("保存购车人信息：orderId={}, personType={}, buyerName={}, idType={}", orderId, personType, buyerName, idType);
    }

    private void saveOrderAssignment(String orderId, String dealership, String deliveryCenter) {
        OrderAssignmentPo assignmentPo = new OrderAssignmentPo();
        assignmentPo.setAssignmentId(IdUtil.nanoId(15));
        assignmentPo.setOrderId(orderId);
        assignmentPo.setOrderStoreCode(dealership);
        assignmentPo.setDeliveryStoreCode(deliveryCenter);
        assignmentPo.setAssignType("initial");
        assignmentPo.setAssignTime(LocalDateTime.now());
        orderAssignmentRepository.save(assignmentPo);
        log.info("保存订单归属信息：orderId={}, dealership={}, deliveryCenter={}", orderId, dealership, deliveryCenter);
    }

    private void saveOrderTimeline(String orderId, String eventType, String eventName,
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
        log.info("保存订单时间线：orderId={}, eventType={}", orderId, eventType);
    }

    private void saveOrderVehicleSnapshot(Order order, String saleModelCode,
                                           String carlineCode, String modelCode, String variantCode,
                                           String configurationCode, List<String> optionCodes,
                                           SaleModelVariantPolicyPo variantPolicy) {
        // 获取saleModelName从vso_sale_model表
        String saleModelName = saleModelRepository.findBySaleModelCode(saleModelCode)
                .map(SaleModelPo::getModelName)
                .orElse("");
        
        // 获取modelName从vso_sale_model_model_policy表
        String modelName = saleModelModelPolicyRepository
                .findBySaleModelCodeAndModelCode(saleModelCode, modelCode)
                .map(p -> p.getMarketingName() != null ? p.getMarketingName() : "")
                .orElse("");
        
        // 获取版本营销名称和价格
        String variantMarketingName = "";
        BigDecimal variantPrice = BigDecimal.ZERO;
        if (variantPolicy != null) {
            variantMarketingName = variantPolicy.getMarketingName() != null ? variantPolicy.getMarketingName() : "";
            variantPrice = variantPolicy.getVariantPrice() != null ? variantPolicy.getVariantPrice() : BigDecimal.ZERO;
        }
        
        // 获取配置名称从ConfigurationService
        String configurationName = "";
        try {
            ConfigurationResponse configResp = configurationService.getByCode(configurationCode);
            if (configResp != null) {
                configurationName = configResp.getName() != null ? configResp.getName() : "";
            }
        } catch (Exception e) {
            log.warn("获取配置名称失败: configurationCode={}", configurationCode, e);
        }
        
        OrderVehicleSnapshotPo snapshotPo = new OrderVehicleSnapshotPo();
        snapshotPo.setSnapshotId(IdUtil.nanoId(15));
        snapshotPo.setOrderId(order.getId());
        snapshotPo.setSaleModelCode(saleModelCode);
        snapshotPo.setSaleModelName(saleModelName);
        snapshotPo.setCarlineCode(carlineCode);
        snapshotPo.setModelCode(modelCode);
        snapshotPo.setModelName(modelName);
        snapshotPo.setVariantCode(variantCode);
        snapshotPo.setVariantName(variantMarketingName);
        snapshotPo.setConfigurationCode(configurationCode);
        snapshotPo.setConfigurationName(configurationName);
        snapshotPo.setVariantPrice(variantPrice);
        snapshotPo.setOptionCodes(JSONUtil.toJsonStr(optionCodes));
        snapshotPo.setSnapshotVersion(1);
        
        // 保存 Variant 销售策略快照
        if (variantPolicy != null) {
            snapshotPo.setVariantPolicySnapshot(JSONUtil.toJsonStr(variantPolicy));
        }
        
        // 获取 Option 营销名称从vso_sale_model_option_policy
        List<SaleModelOptionPolicyPo> optionPolicies = optionPolicyRepository
                .findBySaleModelCodeAndOptionCodes(saleModelCode, optionCodes);
        Map<String, String> optionMarketingNameMap = optionPolicies.stream()
                .collect(Collectors.toMap(
                        SaleModelOptionPolicyPo::getOptionCode,
                        p -> p.getMarketingTitle() != null ? p.getMarketingTitle() : "",
                        (existing, replacement) -> existing
                ));
        
        // 保存 Option 价格明细
        List<Map<String, Object>> optionBreakdown = new ArrayList<>();
        for (String optionCode : optionCodes) {
            BigDecimal optionPrice = salesPolicyService.getOptionPrice(saleModelCode, optionCode);
            Map<String, Object> item = new HashMap<>();
            item.put("optionCode", optionCode);
            
            // 获取 optionFamilyCode
            String optionFamilyCode = "";
            String optionName = optionMarketingNameMap.getOrDefault(optionCode, "");
            Optional<MdmProjectionOptionPo> optionPoOpt = mdmProjectionService.getOptionOptional(optionCode);
            if (optionPoOpt.isPresent()) {
                optionFamilyCode = optionPoOpt.get().getOptionFamilyCode() != null ? optionPoOpt.get().getOptionFamilyCode() : "";
                if (StrUtil.isBlank(optionName)) {
                    optionName = optionPoOpt.get().getOptionName() != null ? optionPoOpt.get().getOptionName() : "";
                }
            }
            
            // 获取 optionFamilyName从vso_sale_model_option_family_policy
            String optionFamilyName = "";
            if (StrUtil.isNotBlank(optionFamilyCode)) {
                optionFamilyName = optionFamilyPolicyRepository
                        .findBySaleModelCodeAndFamilyCode(saleModelCode, optionFamilyCode)
                        .map(p -> p.getMarketingTitle() != null ? p.getMarketingTitle() : "")
                        .orElse("");
            }
            
            item.put("optionFamilyCode", optionFamilyCode);
            item.put("optionFamilyName", optionFamilyName);
            item.put("optionName", optionName);
            item.put("optionPrice", optionPrice);
            optionBreakdown.add(item);
        }
        snapshotPo.setOptionBreakdown(JSONUtil.toJsonStr(optionBreakdown));
        
        orderVehicleSnapshotRepository.save(snapshotPo);
        log.info("保存订单车型快照：orderId={}, saleModelCode={}, modelCode={}, variantCode={}, configurationCode={}, variantPrice={}", 
                order.getId(), saleModelCode, modelCode, variantCode, configurationCode, variantPrice);
    }

    /**
     * 发起支付（委托支付应用服务：金额取订单快照、活动支付单幂等、Mock 网关隔离）
     */
    public InitiatePaymentResult initiatePayment(InitiatePaymentCmd cmd) {
        return paymentAppService.initiatePayment(cmd);
    }

    private List<EarnestMoneyOrderResult.PaymentChannelInfo> buildPaymentChannelInfoList() {
        PaymentChannelConfig.ChannelInfo defaultChannel = paymentChannelConfig.getDefaultChannelInfo();
        return paymentChannelConfig.getEnabledChannels().stream()
                .map(c -> EarnestMoneyOrderResult.PaymentChannelInfo.builder()
                        .channelCode(c.getCode())
                        .channelName(c.getName())
                        .isDefault(c == defaultChannel)
                        .build())
                .collect(Collectors.toList());
    }

    private List<DownPaymentOrderResult.PaymentChannelInfo> buildDownPaymentChannelInfoList() {
        PaymentChannelConfig.ChannelInfo defaultChannel = paymentChannelConfig.getDefaultChannelInfo();
        return paymentChannelConfig.getEnabledChannels().stream()
                .map(c -> DownPaymentOrderResult.PaymentChannelInfo.builder()
                        .channelCode(c.getCode())
                        .channelName(c.getName())
                        .isDefault(c == defaultChannel)
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 创建补款任务
     */
    private String createSupplementaryPaymentTask(Order order, Money amount) {
        String supplementaryNo = generateSupplementaryNo();
        LocalDateTime expireTime = LocalDateTime.now().plusMinutes(30);

        SupplementaryPaymentPo po = SupplementaryPaymentPo.builder()
                .supplementaryNo(supplementaryNo)
                .orderId(order.getId())
                .supplementaryAmount(amount.getAmount())
                .currency(amount.getCurrency())
                .supplementaryStatus(SupplementaryPaymentStatus.PENDING.getValue())
                .configVersionNo(order.getCurrentVersionNo())
                .expireTime(expireTime)
                .build();

        supplementaryPaymentRepository.save(po);
        return supplementaryNo;
    }

    /**
     * 创建改配退款任务
     */
    private String createConfigChangeRefundTask(Order order, Money amount) {
        String refundTaskNo = generateRefundTaskNo();

        ConfigChangeRefundPo po = ConfigChangeRefundPo.builder()
                .refundTaskNo(refundTaskNo)
                .orderId(order.getId())
                .refundAmount(amount.getAmount())
                .currency(amount.getCurrency())
                .refundStatus(ConfigChangeRefundStatus.PENDING.getValue())
                .configVersionNo(order.getCurrentVersionNo())
                .build();

        configChangeRefundRepository.save(po);

        // 异步发起退款
        asyncProcessConfigChangeRefund(refundTaskNo);

        return refundTaskNo;
    }

    /**
     * 异步处理改配退款
     */
    @Async
    public void asyncProcessConfigChangeRefund(String refundTaskNo) {
        try {
            ConfigChangeRefundPo refundPo = configChangeRefundRepository.findByRefundTaskNo(refundTaskNo)
                    .orElseThrow(() -> new RuntimeException("退款任务不存在: " + refundTaskNo));

            // 更新状态为处理中
            configChangeRefundRepository.updateStatus(refundTaskNo, ConfigChangeRefundStatus.PROCESSING, null, null);

            // 调用支付网关发起退款
            String refundId = paymentAdapter.refund(refundPo.getOrderId(), refundPo.getRefundAmount(), "改配价格差额退款");

            configChangeRefundRepository.updateStatus(refundTaskNo, ConfigChangeRefundStatus.COMPLETED, refundId, null);
            log.info("改配退款成功: refundTaskNo={}", refundTaskNo);
        } catch (Exception e) {
            log.error("处理改配退款异常: refundTaskNo={}", refundTaskNo, e);
            configChangeRefundRepository.updateStatus(refundTaskNo, ConfigChangeRefundStatus.FAILED, null, e.getMessage());
        }
    }

    /**
     * 生成补款单号
     */
    private String generateSupplementaryNo() {
        return "SUP" + System.currentTimeMillis() + String.format("%04d", new java.util.Random().nextInt(10000));
    }

    /**
     * 生成退款任务单号
     */
    private String generateRefundTaskNo() {
        return "CCR" + System.currentTimeMillis() + String.format("%04d", new java.util.Random().nextInt(10000));
    }

    /**
     * 获取订单的补款任务列表
     */
    public List<SupplementaryPaymentVo> getSupplementaryPayments(String accountId, String orderNo) {
        Order order = findOrderById(accountId, orderNo);
        List<SupplementaryPaymentPo> list = supplementaryPaymentRepository.findByOrderId(order.getId());
        return list.stream().map(this::toSupplementaryPaymentVo).collect(Collectors.toList());
    }

    /**
     * 运营（MPT）按业务订单 ID 查询补款任务（只读，不做账号隔离）
     */
    public List<SupplementaryPaymentVo> getSupplementaryPaymentsByOrderId(String orderId) {
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new OrderNotExistException(orderId));
        return supplementaryPaymentRepository.findByOrderId(order.getId()).stream()
                .map(this::toSupplementaryPaymentVo)
                .collect(Collectors.toList());
    }

    /**
     * 发起补款支付
     */
    @Transactional(rollbackFor = Exception.class)
    public void initiateSupplementPayment(String accountId, String supplementaryNo, String paymentChannel) {
        SupplementaryPaymentPo supplementPo = supplementaryPaymentRepository.findBySupplementaryNo(supplementaryNo)
                .orElseThrow(() -> new SupplementPaymentNotExistException(supplementaryNo));

        if (!SupplementaryPaymentStatus.PENDING.getValue().equals(supplementPo.getSupplementaryStatus())) {
            throw new SupplementPaymentStatusException("补缴支付[" + supplementaryNo + "]状态[" + supplementPo.getSupplementaryStatus() + "]不允许");
        }

        if (LocalDateTime.now().isAfter(supplementPo.getExpireTime())) {
            supplementaryPaymentRepository.updateStatus(supplementaryNo, SupplementaryPaymentStatus.EXPIRED, null);
            throw new SupplementPaymentExpiredException(supplementaryNo);
        }

        Order order = orderRepository.findByOrderId(supplementPo.getOrderId())
                .orElseThrow(() -> new OrderNotExistException(supplementPo.getOrderId()));

        Payment payment = Payment.builder()
                .paymentId(IdUtil.nanoId(15))
                .paymentNo("P" + IdUtil.nanoId(12))
                .orderId(order.getId())
                .paymentStage(PaymentStage.DOWN_PAYMENT.name())
                .paymentChannel(paymentChannel)
                .paymentAmount(supplementPo.getSupplementaryAmount())
                .paymentStatus(PaymentStatus.PENDING_PAYMENT)
                .initiatorRole("capp_user")
                .initiatorId(accountId)
                .expireTime(supplementPo.getExpireTime())
                .build();

        paymentRepository.save(payment);

        supplementPo.setPaymentId(payment.getPaymentNo());
        supplementaryPaymentRepository.save(supplementPo);

        paymentAdapter.createPayment(payment.getPaymentNo(), supplementPo.getSupplementaryAmount(), paymentChannel);

        log.info("发起补款支付: supplementaryNo={}, paymentNo={}", supplementaryNo, payment.getPaymentNo());
    }

    /**
     * 转换为 SupplementaryPaymentVo
     */
    private SupplementaryPaymentVo toSupplementaryPaymentVo(SupplementaryPaymentPo po) {
        return SupplementaryPaymentVo.builder()
                .supplementaryNo(po.getSupplementaryNo())
                .orderNo(orderRepository.findByOrderId(po.getOrderId()).map(Order::getOrderNo).orElse(""))
                .supplementaryAmount(po.getSupplementaryAmount())
                .currency(po.getCurrency())
                .supplementaryStatus(po.getSupplementaryStatus())
                .configVersionNo(po.getConfigVersionNo())
                .expireTime(po.getExpireTime())
                .createTime(po.getCreateTime())
                .build();
    }

    private void releaseVehicleAssignmentIfNeeded(Order order, String operateType) {
        if (order.getDeliveryVin() == null || order.getDeliveryVin().isEmpty()) {
            return;
        }

        Optional<VehicleAssignment> assignmentOpt = vehicleAssignmentRepository.findDomainByOrderId(order.getId());
        if (assignmentOpt.isEmpty()) {
            return;
        }

        VehicleAssignment assignment = assignmentOpt.get();
        String reason = "cancel".equals(operateType) ? "ORDER_CANCELLED" : "ORDER_CLOSED";
        assignment.release(reason);
        vehicleAssignmentRepository.saveDomain(assignment);

        vehicleInventoryGateway.releaseVehicleStatus(order.getDeliveryVin());

        saveOrderTimeline(order.getId(), "VEHICLE_ASSIGNMENT_RELEASED", "VIN释放",
                null, null, null, null, null, null, "success", null,
                String.format("VIN: %s, 原因: %s", order.getDeliveryVin(), reason));

        log.info("订单取消/关闭释放VIN：orderId={}, vin={}", order.getId(), order.getDeliveryVin());
    }

}
