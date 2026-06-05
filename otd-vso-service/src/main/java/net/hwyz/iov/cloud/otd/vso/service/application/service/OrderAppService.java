package net.hwyz.iov.cloud.otd.vso.service.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.edd.dictionary.api.service.DictionaryService;
import net.hwyz.iov.cloud.edd.dictionary.api.vo.response.DictionaryResponse;
import net.hwyz.iov.cloud.edd.org.api.service.OrgDealershipService;
import net.hwyz.iov.cloud.edd.org.api.vo.DealershipExService;
import net.hwyz.iov.cloud.edd.vmd.api.service.VmdVehicleModelConfigService;
import net.hwyz.iov.cloud.edd.vmd.api.vo.response.VmdBuildConfigFeatureCodeResponse;
import net.hwyz.iov.cloud.edd.vmd.api.vo.response.VmdBuildConfigResponse;
import net.hwyz.iov.cloud.edd.mdm.api.service.ConfigurationService;
import net.hwyz.iov.cloud.edd.mdm.api.vo.request.ConfigurationByVariantAndOptionCodesRequest;
import net.hwyz.iov.cloud.edd.mdm.api.vo.response.ConfigurationResponse;
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
import net.hwyz.iov.cloud.otd.vso.service.common.exception.OrderStateNotAllowedException;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.PaymentChannelNotAvailableException;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.SaleModelNotExistException;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.WishlistNotExistException;
import net.hwyz.iov.cloud.otd.vso.service.domain.service.MdmProjectionService;
import net.hwyz.iov.cloud.otd.vso.service.domain.service.SalesPolicyService;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Order;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderAmount;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderState;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.VehicleAssignment;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Wishlist;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.shared.CustomerInfo;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.shared.Money;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.shared.OrganizationInfo;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.shared.VehicleInfo;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderPartyRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderAssignmentRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderAmountRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderRepository;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        List<Order> orderList = orderRepository.search(
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

        // 4. 组装展示信息
        for (OrderListResult result : results) {
            // 基本信息
            result.setOrderTypeName(getOrderTypeName(result.getOrderType()));
            result.setOrderSourceName(getOrderSourceName(result.getOrderSource()));
            result.setBrandName(result.getBrandCode());
            result.setOwnerRegionName(getRegionName(result.getOwnerRegionCode()));

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
        orderLockService.executeWithLock(cmd.getOrderNo(), cmd.getVin(), "bindVehicle", () -> {
            orderValidationService.validateVinAvailable(cmd.getVin(), cmd.getOrderNo());
            Order order = orderDomainService.loadOrder(cmd.getOrderNo());
            order.saveDeliveryVehicle(cmd.getVin());
            orderRepository.save(order);
        });
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
        orderRepository.save(order);
        
        saveOrderParty(order.getId(), cmd.getAccountId(), "order_user");
        
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
        
        duplicateOrderSpecification.check(cmd.getAccountId(), null);
        
        String configurationCode;
        String saleModel;
        Order order;
        
        if (StrUtil.isNotBlank(cmd.getOrderNo())) {
            Optional<Order> orderOpt = orderRepository.findByOrderNoAndAccountId(cmd.getOrderNo(), cmd.getAccountId());
            if (orderOpt.isPresent()) {
                order = orderOpt.get();
                if (order.getOrderState() == OrderState.EARNEST_MONEY_PAID) {
                    log.info("意向金转定金：orderNo={}, saleModel={}, configurationCode={}", 
                            order.getOrderNo(), order.getSaleModel(), order.getConfigurationCode());
                    saleModel = order.getSaleModel();
                    configurationCode = order.getConfigurationCode();
                } else {
                    log.warn("订单状态不允许定金下单：orderNo={}, orderState={}", 
                            order.getOrderNo(), order.getOrderState());
                    throw new OrderStateNotAllowedException("车辆销售订单[" + order.getOrderNo() + "]当前状态[" + order.getOrderState() + "]不允许定金下单");
                }
            } else {
                order = Order.fromWishlist(cmd.getAccountId(), null);
                saleModel = cmd.getSaleModel();
                configurationCode = cmd.getConfigurationCode();
                order.saveSaleModel(saleModel);
            }
        } else {
            order = Order.fromWishlist(cmd.getAccountId(), null);
            saleModel = cmd.getSaleModel();
            configurationCode = cmd.getConfigurationCode();
            order.saveSaleModel(saleModel);
        }
        
        if (configurationCode == null || configurationCode.isEmpty()) {
            if (cmd.getOptionCodes() != null && !cmd.getOptionCodes().isEmpty()) {
                // 调用 MDM 服务获取 configurationCode
                configurationCode = resolveConfiguration(cmd.getVariantCode(), cmd.getOptionCodes());
            }
        }
        
        if (configurationCode == null || configurationCode.isEmpty()) {
            throw new BuildConfigNotMatchedException(saleModel);
        }
        
        order.saveBrandCode(cmd.getCarlineCode());
        
        order.downPaymentOrder();
        order.saveConfiguration(configurationCode, cmd.getModelConfigMap());
        
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
        
        wishlistRepository.deleteByUserId(cmd.getAccountId());
        log.info("定金下单成功后删除心愿单：accountId={}, orderNo={}", cmd.getAccountId(), order.getOrderNo());
        
        BigDecimal downPaymentAmount = getDownPaymentAmount(saleModel);
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

    public PayResult pay(PayCmd cmd) {
        Order order = findOrderById(cmd.getAccountId(), cmd.getOrderNo());
        OrderState previousState = order.getOrderState();
        
        order.pay(cmd.getPaymentAmount());
        orderRepository.save(order);
        
        if (previousState == OrderState.DOWN_PAYMENT_UNPAID && order.getOrderState() == OrderState.DOWN_PAYMENT_PAID) {
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
        orderLockService.executeWithLock(cmd.getOrderNo(), cmd.getAccountId(), "refund", () -> {
            Order order = findOrderById(cmd.getAccountId(), cmd.getOrderNo());
            
            // 检查是否可以退款
            if (!order.canRefund()) {
                log.warn("订单状态不允许退款：orderNo={}, orderState={}", 
                        order.getOrderNo(), order.getOrderState());
                throw new OrderStateNotAllowedException("车辆销售订单[" + order.getOrderNo() + "]当前状态[" + order.getOrderState() + "]不允许退款");
            }
            
            // 计算退款金额
            Money refundAmount = order.getRefundAmount();
            Money refundFee = order.getRefundFee();
            String refundScene = order.getRefundScene();
            
            // 更新订单状态
            order.requestRefund();
            orderRepository.save(order);
            
            // 创建退款记录
            RefundPo refundPo = new RefundPo();
            refundPo.setRefundId(IdUtil.fastSimpleUUID());
            refundPo.setRefundNo(generateRefundNo());
            refundPo.setOrderId(order.getId());
            refundPo.setRefundScene(refundScene);
            refundPo.setRefundAmount(refundAmount.getAmount());
            refundPo.setRefundFee(refundFee.getAmount());
            refundPo.setRefundStatus("pending");
            refundPo.setApplyTime(LocalDateTime.now());
            refundRepository.save(refundPo);
            
            // 记录时间线
            saveOrderTimeline(order.getId(), "REFUND_APPLY", "申请退款", 
                    order.getOrderState().name(), "REFUND_APPLY",
                    cmd.getAccountId(), "user", "mobile", 
                    refundPo.getRefundNo(), "success", null, 
                    "退款申请成功，退款金额：" + refundAmount + "，手续费：" + refundFee);
            
            log.info("创建退款任务：orderId={}, refundNo={}, refundAmount={}, refundFee={}", 
                    order.getId(), refundPo.getRefundNo(), refundAmount, refundFee);
        });
    }

    /**
     * 生成退款单号
     */
    private String generateRefundNo() {
        return "REF" + System.currentTimeMillis() + String.format("%04d", new java.util.Random().nextInt(10000));
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
                // 差额 > 0：创建差额支付任务
                SupplementaryPaymentPo supplementaryPayment = createEarnestToDownSupplementaryPayment(
                    order.getId(), difference);

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
            .expireTime(expireTime)
            .build();

        supplementaryPaymentRepository.save(supplementaryPayment);
        return supplementaryPayment;
    }

    public void lock(LockCmd cmd) {
        Order order = findOrderById(cmd.getAccountId(), cmd.getOrderNo());
        order.lock();
        orderRepository.save(order);
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
            // 1. 查找订单
            Order order = findOrderById(cmd.getAccountId(), cmd.getOrderNo());

            // 2. 校验订单状态
            validateOrderStateForModifyConfig(order);

            // 3. 获取新的 buildConfigCode（通过 variantCode + optionCodes）
            String newBuildConfigCode = resolveConfiguration(cmd.getVariantCode(), cmd.getOptionCodes());
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
            if (StrUtil.isNotBlank(cmd.getVariantCode())) {
                variantPrice = saleModelVariantPolicyRepository
                    .findBySaleModelCodeAndVariantCode(order.getSaleModel(), cmd.getVariantCode())
                    .map(policy -> policy.getVariantPrice() != null ? policy.getVariantPrice() : BigDecimal.ZERO)
                    .orElse(BigDecimal.ZERO);
            }

            BigDecimal optionTotalPrice = BigDecimal.ZERO;
            if (cmd.getOptionCodes() != null && !cmd.getOptionCodes().isEmpty()) {
                List<SaleModelOptionPolicyPo> optionPolicies = optionPolicyRepository
                    .findBySaleModelCodeAndOptionCodes(order.getSaleModel(), cmd.getOptionCodes());
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
            saveOrderVehicleSnapshotWithVersionIncrement(order, order.getSaleModel(), saleModelName, cmd.getVariantCode(), buildConfig);

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
                    .accountId(cmd.getAccountId())
                    .priceDifference(priceDifference.getAmount())
                    .newConfigVersionNo(order.getCurrentVersionNo())
                    .supplementaryNo(supplementaryNo)
                    .refundTaskNo(refundTaskNo)
                    .build());

            // 14. 记录操作时间线
            saveOrderTimeline(order.getId(), "MODIFY_CONFIG", "修改订单配置",
                    oldConfigurationCode, newBuildConfigCode,
                    cmd.getAccountId(), "order_user", "capp",
                    null, "success", null,
                    String.format("旧配置: %s, 新配置: %s, 价格差额: %s", oldConfigurationCode, newBuildConfigCode, priceDifference));
        });
    }

    private void validateOrderStateForModifyConfig(Order order) {
        OrderState state = order.getOrderState();
        if (state != OrderState.EARNEST_MONEY_PAID 
                && state != OrderState.DOWN_PAYMENT_UNPAID 
                && state != OrderState.DOWN_PAYMENT_PAID) {
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
        order.prepareTransport();
        orderRepository.save(order);
        timeoutNotifyService.createTimeoutTask(order.getId(), "PREPARE_TRANSPORT_TIMEOUT", "remind", 1440);
    }

    public void transporting(TransportingCmd cmd) {
        Order order = findOrderByOrderNo(cmd.getOrderNo());
        timeoutNotifyService.cancelByOrderIdAndType(order.getId(), "PREPARE_TRANSPORT_TIMEOUT");
        order.transporting();
        orderRepository.save(order);
        timeoutNotifyService.createTimeoutTask(order.getId(), "TRANSPORTING_TIMEOUT", "remind", 2880);
    }

    public void prepareDelivery(PrepareDeliveryCmd cmd) {
        Order order = findOrderByOrderNo(cmd.getOrderNo());
        timeoutNotifyService.cancelByOrderIdAndType(order.getId(), "TRANSPORTING_TIMEOUT");
        order.prepareDelivery();
        orderRepository.save(order);
        timeoutNotifyService.createTimeoutTask(order.getId(), "PREPARE_DELIVER_TIMEOUT", "remind", 1440);
    }

    public void delivered(DeliveredCmd cmd) {
        Order order = findOrderByOrderNo(cmd.getOrderNo());
        timeoutNotifyService.cancelByOrderIdAndType(order.getId(), "PREPARE_DELIVER_TIMEOUT");
        order.delivered();
        orderRepository.save(order);
        timeoutNotifyService.createTimeoutTask(order.getId(), "DELIVERED_TIMEOUT", "remind", 4320);
    }

    public void activate(ActivateCmd cmd) {
        Order order = findOrderByOrderNo(cmd.getOrderNo());
        timeoutNotifyService.cancelByOrderIdAndType(order.getId(), "DELIVERED_TIMEOUT");
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
        orderAmountRepository.findByOrderId(order.getId())
                .ifPresentOrElse(
                    amountPo -> order.setOrderAmount(convertAmountToDomain(amountPo)),
                    () -> log.warn("订单金额记录不存在：orderId={}, orderNo={}", order.getId(), order.getOrderNo())
                );
    }

    private OrderAmount convertAmountToDomain(OrderAmountPo po) {
        OrderAmount amount = new OrderAmount(po.getAmountId());
        amount.setGuidePrice(new Money(po.getGuidePrice()));
        amount.setVehiclePrice(new Money(po.getVehiclePrice()));
        amount.setOptionPrice(new Money(po.getOptionPrice()));
        amount.setColorMarkup(new Money(po.getColorMarkup()));
        amount.setServiceFee(new Money(po.getServiceFee()));
        amount.setPlateServiceFee(new Money(po.getPlateServiceFee()));
        amount.setInsuranceFee(new Money(po.getInsuranceFee()));
        amount.setDiscountTotal(new Money(po.getDiscountTotal()));
        amount.setSubsidyTotal(new Money(po.getSubsidyTotal()));
        amount.setFinanceDiscountTotal(new Money(po.getFinanceDiscountTotal()));
        amount.setDealPriceTotal(new Money(po.getDealPriceTotal()));
        amount.setDepositAmount(new Money(po.getDepositAmount()));
        amount.setDownPaymentAmount(new Money(po.getDownPaymentAmount()));
        amount.setTailPaymentAmount(new Money(po.getTailPaymentAmount()));
        amount.setPaidTotal(new Money(po.getPaidTotal()));
        amount.setRefundTotal(new Money(po.getRefundTotal()));
        amount.setReceivableTotal(new Money(po.getReceivableTotal()));
        amount.setNetReceivableTotal(new Money(po.getNetReceivableTotal()));
        amount.setUnpaidTotal(new Money(po.getUnpaidTotal()));
        amount.setInvoiceAmount(new Money(po.getInvoiceAmount()));
        amount.setCalculationVersion(po.getCalculationVersion());
        return amount;
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

    public InitiatePaymentResult initiatePayment(InitiatePaymentCmd cmd) {
        log.info("发起支付：accountId={}, orderNo={}, paymentChannel={}", 
                cmd.getAccountId(), cmd.getOrderNo(), cmd.getPaymentChannel());
        
        return orderLockService.executeWithLock(cmd.getOrderNo(), cmd.getAccountId(), "payment", () -> {
            Optional<Order> orderOpt = orderRepository.findByOrderNo(cmd.getOrderNo());
            if (orderOpt.isEmpty()) {
                throw new OrderNotExistException(cmd.getOrderNo());
            }
            Order order = orderOpt.get();
            
            if (order.getOrderState() != OrderState.EARNEST_MONEY_UNPAID 
                    && order.getOrderState() != OrderState.DOWN_PAYMENT_UNPAID) {
                throw new OrderNotExistException("订单状态不允许支付");
            }
            
            if (!paymentChannelConfig.isChannelEnabled(cmd.getPaymentChannel())) {
                throw new PaymentChannelNotAvailableException(cmd.getPaymentChannel().name());
            }
            
            PaymentStage paymentStage;
            BigDecimal paymentAmount;
            if (order.getOrderState() == OrderState.EARNEST_MONEY_UNPAID) {
                paymentStage = PaymentStage.EARNEST_MONEY;
                paymentAmount = getEarnestMoneyAmount(order.getSaleModel());
            } else {
                paymentStage = PaymentStage.DOWN_PAYMENT;
                paymentAmount = getDownPaymentAmount(order.getSaleModel());
            }
            
            PaymentPo paymentPo = new PaymentPo();
            paymentPo.setPaymentId(IdUtil.nanoId(15));
            paymentPo.setPaymentNo("P" + IdUtil.nanoId(12));
            paymentPo.setOrderId(order.getId());
            paymentPo.setPaymentStage(paymentStage.name());
            paymentPo.setPaymentChannel(cmd.getPaymentChannel().name());
            paymentPo.setPaymentAmount(paymentAmount);
            paymentPo.setPaymentStatus(PaymentStatus.PENDING_PAYMENT.name());
            paymentPo.setInitiatorRole("capp_user");
            paymentPo.setInitiatorId(cmd.getAccountId());
            paymentPo.setAuthorizedFlag(0);
            
            paymentRepository.save(paymentPo);
            
            log.info("支付记录创建成功：paymentNo={}, orderId={}, paymentStage={}, paymentAmount={}", 
                    paymentPo.getPaymentNo(), order.getId(), paymentStage, paymentAmount);
            
            return InitiatePaymentResult.builder()
                    .paymentNo(paymentPo.getPaymentNo())
                    .paymentChannel(cmd.getPaymentChannel())
                    .paymentAmount(paymentAmount)
                    .paymentMerchant(null)
                    .paymentReference(null)
                    .build();
        });
    }

    private BigDecimal getEarnestMoneyAmount(String saleModelCode) {
        // 根据 CR-011 要求，意向金价格从命中的 Variant 销售策略行获取
        // 取该 SaleModel 下当前可售 Variant 销售策略行 earnestMoneyPrice 的最低值
        List<SaleModelVariantPolicyPo> variantPolicies = saleModelVariantPolicyRepository.findBySaleModelCode(saleModelCode);
        return variantPolicies.stream()
            .filter(p -> "active".equals(p.getSaleStatus()))
            .filter(p -> p.getEarnestMoneyPrice() != null)
            .map(SaleModelVariantPolicyPo::getEarnestMoneyPrice)
            .min(BigDecimal::compareTo)
            .orElse(BigDecimal.ZERO);
    }

    private BigDecimal getDownPaymentAmount(String saleModelCode) {
        // 根据 CR-011 要求，定金价格从命中的 Variant 销售策略行获取
        // 取该 SaleModel 下当前可售 Variant 销售策略行 downPaymentPrice 的最低值
        List<SaleModelVariantPolicyPo> variantPolicies = saleModelVariantPolicyRepository.findBySaleModelCode(saleModelCode);
        return variantPolicies.stream()
            .filter(p -> "active".equals(p.getSaleStatus()))
            .filter(p -> p.getDownPaymentPrice() != null)
            .map(SaleModelVariantPolicyPo::getDownPaymentPrice)
            .min(BigDecimal::compareTo)
            .orElse(BigDecimal.ZERO);
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

        PaymentPo paymentPo = new PaymentPo();
        paymentPo.setPaymentId(IdUtil.nanoId(15));
        paymentPo.setPaymentNo("P" + IdUtil.nanoId(12));
        paymentPo.setOrderId(order.getId());
        paymentPo.setPaymentStage(PaymentStage.DOWN_PAYMENT.name());
        paymentPo.setPaymentChannel(paymentChannel);
        paymentPo.setPaymentAmount(supplementPo.getSupplementaryAmount());
        paymentPo.setPaymentStatus(PaymentStatus.PENDING_PAYMENT.name());
        paymentPo.setInitiatorRole("capp_user");
        paymentPo.setInitiatorId(accountId);
        paymentPo.setAuthorizedFlag(0);

        paymentRepository.save(paymentPo);

        supplementPo.setPaymentId(paymentPo.getPaymentNo());
        supplementaryPaymentRepository.save(supplementPo);

        paymentAdapter.createPayment(order.getId(), supplementPo.getSupplementaryAmount(), paymentChannel);

        log.info("发起补款支付: supplementaryNo={}, paymentNo={}", supplementaryNo, paymentPo.getPaymentNo());
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
