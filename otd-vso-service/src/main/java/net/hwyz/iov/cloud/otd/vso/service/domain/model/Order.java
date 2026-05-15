package net.hwyz.iov.cloud.otd.vso.service.domain.model;

import cn.hutool.core.util.IdUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.shared.*;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.OrderIllegalDeleteException;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.OrderStateNotAllowedException;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.SaleModelConfigHasLockedException;
import net.hwyz.iov.cloud.otd.vso.service.common.util.OrderNoGenerator;

import java.math.BigDecimal;
import java.util.*;

/**
 * 订单聚合根
 *
 * @author VSO Team
 */
@Slf4j
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    /** 主键 ID */
    private String id;
    /** 订单号 */
    private String orderNo;
    /** 订单类型 */
    private String orderType;
    /** 订单来源 */
    private String orderSource;
    /** 客户类型 */
    private String customerType;
    /** 付款方式 */
    private String paymentMethod;
    /** 品牌编码 */
    private String brandCode;
    /** 是否有异常单 */
    private Boolean hasException;
    /** 当前版本号 */
    private Integer currentVersionNo;
    /** 是否锁定 */
    private Boolean lockedFlag;
    /** 是否重开 */
    private Boolean reopenFlag;
    /** 订单状态 */
    private OrderState orderState;
    /** 订单状态时间 */
    private Date orderStateTime;
    /** 下单时间 */
    private Date orderTime;
    /** 支付状态 */
    private PayState payState;
    /** 意向金支付时间 */
    private Date earnestMoneyTime;
    /** 意向金支付金额 */
    private BigDecimal earnestMoneyAmount;
    /** 定金支付时间 */
    private Date downPaymentTime;
    /** 定金支付金额 */
    private BigDecimal downPaymentAmount;
    /** 锁单时间 */
    private Date lockTime;
    /** 发运申请时间 */
    private Date transportApplyTime;
    /** 下单人员信息 */
    private CustomerInfo customerInfo;
    /** 组织归属信息 */
    private OrganizationInfo organizationInfo;
    /** 车辆信息 */
    private VehicleInfo vehicleInfo;
    /** 订单金额信息 */
    private OrderAmount orderAmount;
    
    // Legacy ID fields from OrderDo for compatibility during refactoring
    private String orderPersonId;
    private Integer orderPersonType;
    private String orderPersonName;
    private String orderPersonPhone;
    private Integer orderPersonIdType;
    private String orderPersonIdNum;
    private Integer purchasePlan;
    /** 销售车型 */
    private String saleModel;
    /** 生产配置代码 */
    private String buildConfigCode;
    /** 区域代码 */
    private String regionCode;
    /** 门店代码 */
    private String storeCode;
    /** 销售顾问代码 */
    private String salesCode;
    /** 生产配置锁定 */
    private Boolean buildConfigLock;
    private Map<String, OrderModelConfig> modelConfigMap;
    private String licenseCity;
    private String dealership;
    private String deliveryCenter;
    private String transportApplyPersonId;
    private String transportApplyPersonName;
    private String deliveryPersonId;
    private String deliveryPersonName;
    private String deliveryVin;
    private String remark;
    private Boolean valid;

    /** 领域事件 */
    private List<OrderDomainEvent> domainEvents;

    /** 创建时间 */
    private Date createTime;
    /** 修改时间 */
    private Date modifyTime;

    public Order(String id, String orderType, String orderSource) {
        this.id = id;
        generateOrderNo();
        this.orderType = orderType;
        this.orderSource = orderSource;
        this.orderState = OrderState.WISHLIST;
        this.domainEvents = new ArrayList<>();
    }

    /**
     * 初始化
     */
    public void init(String orderPersonId, String orderPersonPhone, String saleModel, OrderState orderState) {
        if (this.id == null) {
            this.id = IdUtil.nanoId(15);
        }
        generateOrderNo();
        if (this.orderType == null) {
            this.orderType = "formal";
        }
        if (this.orderSource == null) {
            this.orderSource = "capp";
        }
        if (this.customerType == null) {
            this.customerType = "personal";
        }
        if (this.hasException == null) {
            this.hasException = false;
        }
        if (this.currentVersionNo == null) {
            this.currentVersionNo = 1;
        }
        if (this.lockedFlag == null) {
            this.lockedFlag = false;
        }
        if (this.reopenFlag == null) {
            this.reopenFlag = false;
        }
        this.orderState = orderState;
        Date now = new Date();
        this.orderStateTime = now;
        if (this.orderState != OrderState.WISHLIST) {
            this.orderTime = now;
        }
        this.buildConfigLock = false;
        this.orderPersonId = orderPersonId;
        this.orderPersonPhone = orderPersonPhone;
        this.saleModel = saleModel;
    }

    /**
     * 生成订单编码
     */
    private void generateOrderNo() {
        this.orderNo = OrderNoGenerator.generate();
    }

    /**
     * 添加领域事件
     */
    protected void addDomainEvent(OrderDomainEvent event) {
        if (this.domainEvents == null) {
            this.domainEvents = new ArrayList<>();
        }
        this.domainEvents.add(event);
    }

    /**
     * 获取并清除领域事件
     */
    public List<OrderDomainEvent> getDomainEvents() {
        List<OrderDomainEvent> events = this.domainEvents == null ? new ArrayList<>() : new ArrayList<>(this.domainEvents);
        if (this.domainEvents != null) {
            this.domainEvents.clear();
        }
        return events;
    }

    // --- 业务方法 (From OrderDo) ---

    public void earnestMoneyOrder() {
        this.orderState = OrderState.EARNEST_MONEY_UNPAID;
        Date now = new Date();
        this.orderStateTime = now;
        this.orderTime = now;
    }

    public void downPaymentOrder() {
        this.orderState = OrderState.DOWN_PAYMENT_UNPAID;
        Date now = new Date();
        this.orderStateTime = now;
        this.orderTime = now;
    }

    public void saveBuildConfig(String buildConfigCode, Map<String, OrderModelConfig> modelConfigMap) {
        if (Boolean.TRUE.equals(buildConfigLock)) {
            throw new SaleModelConfigHasLockedException(orderNo);
        }
        this.buildConfigCode = buildConfigCode;
        this.modelConfigMap = modelConfigMap;
    }

    public void saveBrandCode(String brandCode) {
        this.brandCode = brandCode;
    }

    public void saveRegionCode(String regionCode) {
        this.regionCode = regionCode;
    }

    public void saveSaleModel(String saleModel) {
        this.saleModel = saleModel;
    }

    public void saveStoreCode(String storeCode) {
        this.storeCode = storeCode;
    }

    public void saveSalesCode(String salesCode) {
        this.salesCode = salesCode;
    }

    public void pay(BigDecimal payAmount) {
        Date now = new Date();
        switch (this.orderState) {
            case EARNEST_MONEY_UNPAID -> {
                this.orderState = OrderState.EARNEST_MONEY_PAID;
                this.orderStateTime = now;
                this.earnestMoneyTime = now;
                this.earnestMoneyAmount = payAmount;
                this.payState = PayState.EARNEST_MONEY_PAID;
            }
            case DOWN_PAYMENT_UNPAID -> {
                this.orderState = OrderState.DOWN_PAYMENT_PAID;
                this.orderStateTime = now;
                this.downPaymentTime = now;
                this.downPaymentAmount = payAmount;
                this.payState = PayState.DOWN_PAYMENT_PAID;
            }
            default -> throw new OrderStateNotAllowedException(this.orderNo, this.orderState, "PAY");
        }
    }

    public void lock() {
        if (this.orderState != OrderState.DOWN_PAYMENT_PAID) {
            throw new OrderStateNotAllowedException(this.orderNo, this.orderState, "LOCK");
        }
        this.buildConfigLock = true;
        this.orderState = OrderState.ARRANGE_PRODUCTION;
        Date now = new Date();
        this.orderStateTime = now;
        this.lockTime = now;
    }

    public void cancel() {
        this.orderState = OrderState.CANCEL;
        this.orderStateTime = new Date();
    }

    public void markDelete() {
        this.orderState = OrderState.CANCEL;
        this.orderStateTime = new Date();
    }

    public boolean manageDelete() {
        if (this.orderState != OrderState.CANCEL) {
            throw new OrderIllegalDeleteException(this.orderNo);
        }
        return true;
    }

    public void requestRefund() {
        this.orderState = OrderState.REFUND_APPLY;
        this.orderStateTime = new Date();
    }

    public void earnestMoneyToDownPayment() {
        this.orderState = OrderState.DOWN_PAYMENT_PAID;
        this.orderStateTime = new Date();
    }

    public void saveOrderPerson(String orderPersonId, Integer orderPersonType, String orderPersonName,
                                 Integer orderPersonIdType, String orderPersonIdNum) {
        this.orderPersonId = orderPersonId;
        this.orderPersonType = orderPersonType;
        this.orderPersonName = orderPersonName;
        this.orderPersonIdType = orderPersonIdType;
        this.orderPersonIdNum = orderPersonIdNum;
    }

    public void savePurchasePlan(Integer purchasePlan) {
        this.purchasePlan = purchasePlan;
    }

    public void saveLicenseCity(String licenseCity) {
        this.licenseCity = licenseCity;
    }

    public void saveCustomerType(String customerType) {
        this.customerType = customerType;
    }

    public void savePaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public void saveDealership(String dealership) {
        this.dealership = dealership;
    }

    public void saveDeliveryCenter(String deliveryCenter) {
        this.deliveryCenter = deliveryCenter;
    }

    public void saveDeliveryPerson(String deliveryPersonId, String deliveryPersonName) {
        this.deliveryPersonId = deliveryPersonId;
        this.deliveryPersonName = deliveryPersonName;
    }

    public void saveDeliveryVehicle(String deliveryVin) {
        this.deliveryVin = deliveryVin;
    }

    public void applyTransportVehicle(String transportApplyPersonId, String transportApplyPersonName) {
        this.transportApplyPersonId = transportApplyPersonId;
        this.transportApplyPersonName = transportApplyPersonName;
        this.transportApplyTime = new Date();
    }

    public void prepareTransport() {
        this.orderState = OrderState.PREPARE_TRANSPORT;
        this.orderStateTime = new Date();
    }

    public void transporting() {
        this.orderState = OrderState.TRANSPORTING;
        this.orderStateTime = new Date();
    }

    public void prepareDelivery() {
        this.orderState = OrderState.PREPARE_DELIVER;
        this.orderStateTime = new Date();
    }

    public void delivered() {
        this.orderState = OrderState.DELIVERED;
        this.orderStateTime = new Date();
    }

    public void setCustomerInfo(CustomerInfo customerInfo) {
        this.customerInfo = customerInfo;
    }

    public void setVehicleInfo(VehicleInfo vehicleInfo) {
        this.vehicleInfo = vehicleInfo;
    }

    public void setOrganizationInfo(OrganizationInfo organizationInfo) {
        this.organizationInfo = organizationInfo;
    }

    public void setOrderAmount(OrderAmount orderAmount) {
        this.orderAmount = orderAmount;
    }

    public void submit() {
        // TODO: 实现提交逻辑
    }

    public void auditPass() {
        // TODO: 实现审核通过逻辑
    }

    public void auditReject(String reason) {
        // TODO: 实现审核驳回逻辑
    }

    public void cancel(String reason) {
        this.orderState = OrderState.CANCEL;
        this.orderStateTime = new Date();
    }

    public void invalidate() {
        this.orderState = OrderState.EXPIRED;
        this.orderStateTime = new Date();
    }

    public void close(String reason) {
        // TODO: 实现关闭逻辑
    }

    public String getMainStatus() {
        return this.orderState != null ? this.orderState.name() : null;
    }

    public void complete() {
        // TODO: 实现完成逻辑
    }

    public static Order createSmallOrder(String orderId, String orderSource) {
        Order order = new Order();
        order.id = orderId;
        order.orderSource = orderSource;
        return order;
    }

    public static Order createFormalOrder(String orderId, String orderSource) {
        Order order = new Order();
        order.id = orderId;
        order.orderSource = orderSource;
        return order;
    }

    public void activate() {
        this.orderState = OrderState.ACTIVATED;
        this.orderStateTime = new Date();
    }

    public static Order fromWishlist(String accountId, String saleModel) {
        Order order = new Order();
        order.id = IdUtil.nanoId(15);
        order.orderType = "small";
        order.orderSource = "capp";
        order.customerType = "personal";
        order.hasException = false;
        order.currentVersionNo = 1;
        order.lockedFlag = false;
        order.reopenFlag = false;
        order.orderPersonId = accountId;
        order.saleModel = saleModel;
        order.orderState = OrderState.WISHLIST;
        return order;
    }

    /**
     * 意向金下单（小订单创建）
     */
    public void createSmallOrder() {
        if (this.orderType == null || this.orderType.equals("small")) {
            this.orderType = "small";
            generateOrderNo();
        }
        this.orderState = OrderState.EARNEST_MONEY_UNPAID;
        Date now = new Date();
        this.orderStateTime = now;
        this.orderTime = now;
    }

}
