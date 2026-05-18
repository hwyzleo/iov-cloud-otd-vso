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
import net.hwyz.iov.cloud.framework.web.util.PageUtil;
import net.hwyz.iov.cloud.otd.vso.api.enums.CustomerType;
import net.hwyz.iov.cloud.otd.vso.api.enums.PaymentChannel;
import net.hwyz.iov.cloud.otd.vso.api.enums.PaymentMethod;
import net.hwyz.iov.cloud.otd.vso.api.enums.PaymentStage;
import net.hwyz.iov.cloud.otd.vso.api.enums.PaymentStatus;
import net.hwyz.iov.cloud.otd.vso.service.adapter.web.assembler.OrderDtoAssembler;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.*;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.query.CountQuery;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.query.OrderQuery;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.*;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.BrandCodeNotExistException;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.BuildConfigNotMatchedException;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.OrderNotExistException;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.OrderStateNotAllowedException;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.PaymentChannelNotAvailableException;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.WishlistNotExistException;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Order;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderAmount;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderState;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Wishlist;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.shared.CustomerInfo;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.shared.OrganizationInfo;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.shared.VehicleInfo;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderPartyRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderAssignmentRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.PaymentRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.SaleModelRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.WishlistRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.AuditRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderVehicleSnapshotRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.service.OrderDomainService;
import net.hwyz.iov.cloud.otd.vso.service.domain.service.OrderLockService;
import net.hwyz.iov.cloud.otd.vso.service.domain.service.OrderPhysicalDeleteService;
import net.hwyz.iov.cloud.otd.vso.service.domain.service.OrderValidationService;
import net.hwyz.iov.cloud.otd.vso.service.domain.service.TimeoutNotifyService;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.config.PaymentChannelConfig;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderAssignmentPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderPartyPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderTimelinePo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderVehicleSnapshotPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.PaymentPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelPo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
    private final OrderPartyRepository orderPartyRepository;
    private final OrderAssignmentRepository orderAssignmentRepository;
    private final WishlistRepository wishlistRepository;
    private final VmdVehicleModelConfigService vmdVehicleModelConfigService;
    private final OrderPhysicalDeleteService orderPhysicalDeleteService;
    private final PaymentChannelConfig paymentChannelConfig;
    private final SaleModelRepository saleModelRepository;
    private final PaymentRepository paymentRepository;
    private final SaleModelAppService saleModelAppService;
    private final DictionaryService dictionaryService;
    private final OrgDealershipService orgDealershipService;
    private final AuditRepository auditRepository;
    private final OrderVehicleSnapshotRepository orderVehicleSnapshotRepository;

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
        List<OrderListResult> results = PageUtil.convert(orderList, OrderDtoAssembler.INSTANCE::toOrderListResult);
        
        enrichOrderListResults(results);
        
        return results;
    }

    private void enrichOrderListResults(List<OrderListResult> results) {
        if (results == null || results.isEmpty()) {
            return;
        }

        Set<String> buildConfigCodes = results.stream()
                .map(OrderListResult::getBuildConfigCode)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toSet());
        Set<String> saleModels = results.stream()
                .map(OrderListResult::getSaleModel)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toSet());

        Map<String, String> buildConfigNameMap = new HashMap<>();
        for (String code : buildConfigCodes) {
            try {
                VmdBuildConfigResponse buildConfig = vmdVehicleModelConfigService.getBuildConfigByCode(code);
                if (buildConfig != null) {
                    buildConfigNameMap.put(code, buildConfig.getName());
                }
            } catch (Exception e) {
                log.warn("获取生产配置名称失败: buildConfigCode={}", code);
            }
        }

        Map<String, String> saleModelNameMap = new HashMap<>();
        for (String saleModelCode : saleModels) {
            try {
                Optional<SaleModelPo> saleModelPo = saleModelRepository.findBySaleModelCode(saleModelCode);
                if (saleModelPo.isPresent()) {
                    saleModelNameMap.put(saleModelCode, saleModelPo.get().getModelName());
                }
            } catch (Exception e) {
                log.warn("获取销售车型名称失败: saleModelCode={}", saleModelCode);
            }
        }

        for (OrderListResult result : results) {
            result.setOrderTypeName(getOrderTypeName(result.getOrderType()));
            result.setOrderSourceName(getOrderSourceName(result.getOrderSource()));
            result.setBrandName(result.getBrandCode());
            result.setSaleModelName(saleModelNameMap.getOrDefault(result.getSaleModel(), ""));
            result.setOwnerRegionName(getRegionName(result.getOwnerRegionCode()));

            if (StrUtil.isNotBlank(result.getSaleModel()) && StrUtil.isNotBlank(result.getBuildConfigCode())) {
                try {
                    Map<String, String> featureCodes = parseBuildConfigToFeatureCodes(result.getBuildConfigCode());
                    SelectedSaleModelResult selectedModel = saleModelAppService.getSelectedSaleModelByFeatureCodes(
                            result.getSaleModel(), featureCodes);

                    result.setSaleModelConfigType(selectedModel.getSaleModelConfigType());
                    result.setSaleModelConfigName(selectedModel.getSaleModelConfigName());
                    result.setSaleModelImages(selectedModel.getSaleModelImages());
                    result.setTotalPrice(selectedModel.getTotalPrice());
                    result.setSaleModelDesc(selectedModel.getSaleModelDesc());
                } catch (Exception e) {
                    log.warn("获取销售车型配置信息失败: saleModel={}, buildConfigCode={}", 
                            result.getSaleModel(), result.getBuildConfigCode(), e);
                }
            }
        }
    }

    private Map<String, String> parseBuildConfigToFeatureCodes(String buildConfigCode) {
        Map<String, String> featureCodes = new HashMap<>();

        try {
            VmdBuildConfigResponse buildConfig = vmdVehicleModelConfigService.getBuildConfigByCode(buildConfigCode);

            if (buildConfig != null && buildConfig.getFeatureCodes() != null) {
                for (VmdBuildConfigFeatureCodeResponse fc : buildConfig.getFeatureCodes()) {
                    String familyCode = fc.getFamilyCode();
                    if (fc.getFeatureCode() != null && fc.getFeatureCode().length > 0) {
                        featureCodes.put(familyCode, fc.getFeatureCode()[0]);
                    }
                }

                if (buildConfig.getBaseModelCode() != null && !buildConfig.getBaseModelCode().isEmpty()) {
                    featureCodes.put("BASE_MODEL", buildConfig.getBaseModelCode());
                }
            }
        } catch (Exception e) {
            log.warn("解析生产配置失败: buildConfigCode={}", buildConfigCode, e);
        }

        return featureCodes;
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

    public EarnestMoneyOrderResult earnestMoneyOrder(EarnestMoneyCmd cmd) {
        log.info("意向金下单：accountId={}, saleModel={}, featureConfig={}", 
                cmd.getAccountId(), cmd.getSaleModel(), cmd.getFeatureConfig());
        
        Order order = createOrFindOrder(cmd.getAccountId(), cmd.getOrderNo());
        
        String buildConfigCode = null;
        String saleModel = cmd.getSaleModel();
        
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
            throw new BuildConfigNotMatchedException(saleModel);
        }
        
        order.saveBuildConfig(buildConfigCode, cmd.getModelConfigMap());
        VmdBuildConfigResponse buildConfig = vmdVehicleModelConfigService.getBuildConfigByCode(buildConfigCode);
        log.info("VMD返回buildConfig详情={}", buildConfig);
        if (buildConfig == null || buildConfig.getBrandCode() == null) {
            throw new BrandCodeNotExistException(buildConfigCode);
        }
        order.saveBrandCode(buildConfig.getBrandCode());
        order.saveSaleModel(saleModel);
        String saleModelName = saleModelRepository.findBySaleModelCode(saleModel)
                .map(SaleModelPo::getModelName)
                .orElse("");
        saveOrderVehicleSnapshot(order, saleModel, saleModelName, buildConfig);
        
        order.earnestMoneyOrder();
        order.saveLicenseCity(cmd.getLicenseCityCode());
        orderRepository.save(order);
        
        saveOrderParty(order.getId(), cmd.getAccountId(), "order_user");
        
        wishlistRepository.deleteByUserId(cmd.getAccountId());
        log.info("意向金下单成功后删除心愿单：accountId={}, orderNo={}", cmd.getAccountId(), order.getOrderNo());
        
        timeoutNotifyService.createTimeoutTask(order.getId(), "SMALL_ORDER_PAY_TIMEOUT", "invalid", 
                paymentChannelConfig.getSmallOrderTimeoutMinutes());
        
        BigDecimal earnestMoneyAmount = getEarnestMoneyAmount(saleModel);
        Instant expireTime = Instant.now().plusSeconds(paymentChannelConfig.getSmallOrderTimeoutMinutes() * 60);
        List<EarnestMoneyOrderResult.PaymentChannelInfo> paymentChannels = buildPaymentChannelInfoList();
        
        log.info("意向金下单完成：orderId={}, orderNo={}, buildConfigCode={}, earnestMoneyAmount={}", 
                order.getId(), order.getOrderNo(), order.getBuildConfigCode(), earnestMoneyAmount);
        
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
        
        String buildConfigCode;
        String saleModel;
        Order order;
        
        if (StrUtil.isNotBlank(cmd.getOrderNo())) {
            Optional<Order> orderOpt = orderRepository.findByOrderNoAndAccountId(cmd.getOrderNo(), cmd.getAccountId());
            if (orderOpt.isPresent()) {
                order = orderOpt.get();
                if (order.getOrderState() == OrderState.EARNEST_MONEY_PAID) {
                    log.info("意向金转定金：orderNo={}, saleModel={}, buildConfigCode={}", 
                            order.getOrderNo(), order.getSaleModel(), order.getBuildConfigCode());
                    saleModel = order.getSaleModel();
                    buildConfigCode = order.getBuildConfigCode();
                } else {
                    log.warn("订单状态不允许定金下单：orderNo={}, orderState={}", 
                            order.getOrderNo(), order.getOrderState());
                    throw new OrderStateNotAllowedException(order.getOrderNo(), order.getOrderState(), "DOWN_PAYMENT");
                }
            } else {
                order = Order.fromWishlist(cmd.getAccountId(), null);
                saleModel = cmd.getSaleModel();
                buildConfigCode = cmd.getBuildConfigCode();
                order.saveSaleModel(saleModel);
            }
        } else {
            order = Order.fromWishlist(cmd.getAccountId(), null);
            saleModel = cmd.getSaleModel();
            buildConfigCode = cmd.getBuildConfigCode();
            order.saveSaleModel(saleModel);
        }
        
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
            throw new BuildConfigNotMatchedException(saleModel);
        }
        
        VmdBuildConfigResponse buildConfig = vmdVehicleModelConfigService.getBuildConfigByCode(buildConfigCode);
        if (buildConfig == null || buildConfig.getBrandCode() == null) {
            throw new BrandCodeNotExistException(buildConfigCode);
        }
        order.saveBrandCode(buildConfig.getBrandCode());
        
        order.downPaymentOrder();
        order.saveBuildConfig(buildConfigCode, cmd.getModelConfigMap());
        
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

        log.info("定金下单完成：orderId={}, orderNo={}, saleModel={}, buildConfigCode={}, downPaymentAmount={}", 
                order.getId(), order.getOrderNo(), saleModel, buildConfigCode, downPaymentAmount);

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
        
        if (StrUtil.isNotBlank(order.getSaleModel()) && StrUtil.isNotBlank(order.getBuildConfigCode())) {
            Map<String, String> featureCodes = parseBuildConfigToFeatureCodes(order.getBuildConfigCode());
            SelectedSaleModelResult selectedModel = saleModelAppService.getSelectedSaleModelByFeatureCodes(
                    order.getSaleModel(), featureCodes);
            
            result.setSaleModelConfigType(selectedModel.getSaleModelConfigType());
            result.setSaleModelConfigName(selectedModel.getSaleModelConfigName());
            result.setSaleModelConfigPrice(selectedModel.getSaleModelConfigPrice());
            result.setSaleModelImages(selectedModel.getSaleModelImages());
            result.setSaleModelDesc(selectedModel.getSaleModelDesc());
            result.setTotalPrice(selectedModel.getTotalPrice());
        }
        
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
        try {
            DictionaryResponse city = dictionaryService.getDictionary("city");
            if (city != null && city.getItems() != null) {
                for (Map<String, Object> item : city.getItems()) {
                    if (item.get("code") != null && item.get("code").toString().equals(cityCode)) {
                        return item.get("name") != null ? item.get("name").toString() : cityCode;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("获取城市名称失败: cityCode={}", cityCode, e);
        }
        return cityCode;
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
        Order order = findOrderById(cmd.getAccountId(), cmd.getOrderNo());
        order.requestRefund();
        orderRepository.save(order);
    }

    @Transactional(rollbackFor = Exception.class)
    public void earnestMoneyToDownPayment(EarnestToDownCmd cmd) {
        log.info("意向金转定金：accountId={}, orderNo={}", cmd.getAccountId(), cmd.getOrderNo());
        
        if (StrUtil.isNotBlank(cmd.getCustomerType()) && !CustomerType.isValid(cmd.getCustomerType())) {
            log.warn("客户类型不合法：customerType={}", cmd.getCustomerType());
            throw new IllegalArgumentException("客户类型不合法，仅支持：personal");
        }
        if (StrUtil.isNotBlank(cmd.getPaymentMethod()) && !PaymentMethod.isValid(cmd.getPaymentMethod())) {
            log.warn("支付方式不合法：paymentMethod={}", cmd.getPaymentMethod());
            throw new IllegalArgumentException("支付方式不合法，仅支持：full_payment、loan");
        }
        
        Order order = findOrderById(cmd.getAccountId(), cmd.getOrderNo());
        
        if (order.getOrderState() != OrderState.EARNEST_MONEY_PAID) {
            log.warn("订单状态不允许转定金：orderNo={}, orderState={}", 
                    order.getOrderNo(), order.getOrderState());
            throw new OrderStateNotAllowedException(order.getOrderNo(), order.getOrderState(), "EARNEST_TO_DOWN_PAYMENT");
        }
        
        OrderState beforeState = order.getOrderState();
        
        order.earnestMoneyToDownPayment();
        
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
        
        if (StrUtil.isNotBlank(cmd.getOrderPersonName()) && StrUtil.isNotBlank(cmd.getOrderPersonIdNum())) {
            saveBuyerInfo(order.getId(), cmd.getOrderPersonType(), cmd.getOrderPersonName(),
                    cmd.getOrderPersonIdType(), cmd.getOrderPersonIdNum());
        }
        
        if (StrUtil.isNotBlank(cmd.getOrderStoreCode()) || StrUtil.isNotBlank(cmd.getDeliveryStoreCode())) {
            saveOrderAssignment(order.getId(), cmd.getOrderStoreCode(), cmd.getDeliveryStoreCode());
        }
        
        saveOrderTimeline(order.getId(), "EARNEST_TO_DOWN_PAYMENT", "意向金转定金",
                beforeState.getValue().toString(), order.getOrderState().getValue().toString(),
                cmd.getAccountId(), "order_user", "capp",
                null, "success", null, 
                "意向金订单转定金订单");
        
        log.info("意向金转定金完成：orderId={}, orderNo={}, orderType={}", 
                order.getId(), order.getOrderNo(), order.getOrderType());
    }

    public void lock(LockCmd cmd) {
        Order order = findOrderById(cmd.getAccountId(), cmd.getOrderNo());
        order.lock();
        orderRepository.save(order);
    }

    @Transactional(rollbackFor = Exception.class)
    public void modifyConfig(ModifyOrderConfigCmd cmd) {
        log.info("修改订单配置：accountId={}, orderNo={}, featureConfig={}", 
                cmd.getAccountId(), cmd.getOrderNo(), cmd.getFeatureConfig());
        
        Order order = findOrderById(cmd.getAccountId(), cmd.getOrderNo());
        
        validateOrderStateForModifyConfig(order);
        
        String newBuildConfigCode = getBuildConfigCodeFromFeatureConfig(cmd.getFeatureConfig());
        if (newBuildConfigCode == null || newBuildConfigCode.isEmpty()) {
            throw new BuildConfigNotMatchedException(order.getSaleModel());
        }
        
        VmdBuildConfigResponse buildConfig = vmdVehicleModelConfigService.getBuildConfigByCode(newBuildConfigCode);
        if (buildConfig == null || buildConfig.getBrandCode() == null) {
            throw new BrandCodeNotExistException(newBuildConfigCode);
        }
        
        String oldBuildConfigCode = order.getBuildConfigCode();
        
        order.saveBuildConfig(newBuildConfigCode, null);
        order.saveBrandCode(buildConfig.getBrandCode());
        
        String saleModelName = saleModelRepository.findBySaleModelCode(order.getSaleModel())
                .map(SaleModelPo::getModelName)
                .orElse("");
        
        saveOrderVehicleSnapshotWithVersionIncrement(order, order.getSaleModel(), saleModelName, buildConfig);
        
        orderRepository.save(order);
        
        saveOrderTimeline(order.getId(), "MODIFY_CONFIG", "修改订单配置",
                oldBuildConfigCode, newBuildConfigCode,
                cmd.getAccountId(), "order_user", "capp",
                null, "success", null, 
                "用户修改订单车辆配置");
        
        log.info("修改订单配置完成：orderId={}, orderNo={}, oldBuildConfig={}, newBuildConfig={}", 
                order.getId(), order.getOrderNo(), oldBuildConfigCode, newBuildConfigCode);
    }

    private void validateOrderStateForModifyConfig(Order order) {
        OrderState state = order.getOrderState();
        if (state != OrderState.EARNEST_MONEY_PAID 
                && state != OrderState.DOWN_PAYMENT_UNPAID 
                && state != OrderState.DOWN_PAYMENT_PAID) {
            throw new OrderStateNotAllowedException(order.getOrderNo(), state, "MODIFY_CONFIG");
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
            String saleModelName, VmdBuildConfigResponse buildConfig) {
        if (StrUtil.isBlank(saleModelCode)) {
            log.warn("订单缺少saleModelCode，无法生成车型快照：orderNo={}", order.getOrderNo());
            return;
        }
        
        if (buildConfig == null || StrUtil.isBlank(buildConfig.getCode())) {
            log.warn("订单缺少buildConfig，无法生成车型快照：orderNo={}", order.getOrderNo());
            return;
        }
        
        Integer currentVersion = orderVehicleSnapshotRepository.findMaxVersionByOrderId(order.getId());
        Integer newVersion = currentVersion + 1;
        
        OrderVehicleSnapshotPo snapshotPo = new OrderVehicleSnapshotPo();
        snapshotPo.setSnapshotId(IdUtil.nanoId(15));
        snapshotPo.setOrderId(order.getId());
        snapshotPo.setSaleModelCode(saleModelCode);
        snapshotPo.setSaleModelName(saleModelName);
        snapshotPo.setBuildConfigCode(buildConfig.getCode());
        snapshotPo.setBuildConfigName(buildConfig.getName());
        snapshotPo.setFeatureConfigSnapshot(JSONUtil.toJsonStr(buildConfig.getFeatureCodes()));
        snapshotPo.setSnapshotVersion(newVersion);
        
        orderVehicleSnapshotRepository.save(snapshotPo);
        log.info("保存订单车型快照(新版本)：orderId={}, version={}, buildConfigCode={}", 
                order.getId(), newVersion, buildConfig.getCode());
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

    private void saveOrderVehicleSnapshot(Order order, String saleModelCode, String saleModelName, VmdBuildConfigResponse buildConfig) {
        if (StrUtil.isBlank(saleModelCode)) {
            log.warn("订单缺少saleModelCode，无法生成车型快照：orderNo={}", order.getOrderNo());
            return;
        }
        
        if (buildConfig == null || StrUtil.isBlank(buildConfig.getCode())) {
            log.warn("订单缺少buildConfig，无法生成车型快照：orderNo={}", order.getOrderNo());
            return;
        }
        
        OrderVehicleSnapshotPo snapshotPo = new OrderVehicleSnapshotPo();
        snapshotPo.setSnapshotId(IdUtil.nanoId(15));
        snapshotPo.setOrderId(order.getId());
        snapshotPo.setSaleModelCode(saleModelCode);
        snapshotPo.setSaleModelName(saleModelName);
        snapshotPo.setBuildConfigCode(buildConfig.getCode());
        snapshotPo.setBuildConfigName(buildConfig.getName());
        snapshotPo.setFeatureConfigSnapshot(JSONUtil.toJsonStr(buildConfig.getFeatureCodes()));
        snapshotPo.setSnapshotVersion(1);
        
        orderVehicleSnapshotRepository.save(snapshotPo);
        log.info("保存订单车型快照：orderId={}, saleModelCode={}, saleModelName={}, buildConfigCode={}, buildConfigName={}", 
                order.getId(), snapshotPo.getSaleModelCode(), snapshotPo.getSaleModelName(),
                snapshotPo.getBuildConfigCode(), snapshotPo.getBuildConfigName());
    }

    public InitiatePaymentResult initiatePayment(InitiatePaymentCmd cmd) {
        log.info("发起支付：accountId={}, orderNo={}, paymentChannel={}", 
                cmd.getAccountId(), cmd.getOrderNo(), cmd.getPaymentChannel());
        
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
    }

    private BigDecimal getEarnestMoneyAmount(String saleModelCode) {
        if (saleModelCode == null || saleModelCode.isEmpty()) {
            return BigDecimal.ZERO;
        }
        Optional<SaleModelPo> saleModelOpt = saleModelRepository.findBySaleModelCode(saleModelCode);
        if (saleModelOpt.isEmpty()) {
            return BigDecimal.ZERO;
        }
        SaleModelPo saleModel = saleModelOpt.get();
        if (saleModel.getEarnestMoneyPrice() == null) {
            return BigDecimal.ZERO;
        }
        return saleModel.getEarnestMoneyPrice();
    }

    private BigDecimal getDownPaymentAmount(String saleModelCode) {
        if (saleModelCode == null || saleModelCode.isEmpty()) {
            return BigDecimal.ZERO;
        }
        Optional<SaleModelPo> saleModelOpt = saleModelRepository.findBySaleModelCode(saleModelCode);
        if (saleModelOpt.isEmpty()) {
            return BigDecimal.ZERO;
        }
        SaleModelPo saleModel = saleModelOpt.get();
        if (saleModel.getDownPaymentPrice() == null) {
            return BigDecimal.ZERO;
        }
        return saleModel.getDownPaymentPrice();
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

}
