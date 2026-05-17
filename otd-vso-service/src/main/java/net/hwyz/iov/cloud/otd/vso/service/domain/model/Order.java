package net.hwyz.iov.cloud.otd.vso.service.domain.model;

import cn.hutool.core.util.IdUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.otd.vso.api.enums.CustomerType;
import net.hwyz.iov.cloud.otd.vso.api.enums.OrderType;
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
 * 车辆销售订单的核心领域对象，封装订单的全部业务逻辑和状态变更规则。
 * 支持从心愿单到交付完成的完整生命周期管理。
 *
 * @author VSO Team
 */
@Slf4j
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    // ==================== 基本标识字段 ====================

    /** 订单唯一标识，系统内部使用 */
    private String id;

    /** 订单号，对外展示的业务编号，格式由OrderNoGenerator生成 */
    private String orderNo;

    /** 订单类型：SMALL(小订单/意向金订单)、FORMAL(正式订单)、MANUAL(手工订单)等 */
    private OrderType orderType;

    /** 订单来源：capp(C端自主下单)、store(门店下单)、sales(销售代客下单)等 */
    private String orderSource;

    /** 客户类型：personal(个人)、corporate(企业) */
    private String customerType;

    /** 付款方式：full(全款)、finance(金融) */
    private String paymentMethod;

    /** 品牌编码 */
    private String brandCode;

    // ==================== 状态控制字段 ====================

    /** 是否有异常单标记，用于标记订单处理过程中的异常情况 */
    private Boolean hasException;

    /** 当前版本号，用于订单变更追踪 */
    private Integer currentVersionNo;

    /** 生产配置锁定标记，锁定后不可修改车辆配置 */
    private Boolean buildConfigLock;

    /** 订单锁定标记，用于防止并发操作 */
    private Boolean lockedFlag;

    /** 订单重开标记，标记订单是否曾被关闭后重新打开 */
    private Boolean reopenFlag;

    /** 订单状态，见OrderState枚举 */
    private OrderState orderState;

    /** 订单状态变更时间 */
    private Date orderStateTime;

    /** 下单时间，订单首次进入有效状态的时间 */
    private Date orderTime;

    /** 支付状态，见PayState枚举 */
    private PayState payState;

    // ==================== 支付相关字段 ====================

    /** 意向金支付时间 */
    private Date earnestMoneyTime;

    /** 意向金支付金额 */
    private BigDecimal earnestMoneyAmount;

    /** 定金支付时间 */
    private Date downPaymentTime;

    /** 定金支付金额 */
    private BigDecimal downPaymentAmount;

    /** 锁单时间，生产配置锁定的时间 */
    private Date lockTime;

    /** 发运申请时间 */
    private Date transportApplyTime;

    // ==================== 嵌入值对象 ====================

    /** 下单人员信息（客户详细信息） */
    private CustomerInfo customerInfo;

    /** 组织归属信息（门店、销售顾问等） */
    private OrganizationInfo organizationInfo;

    /** 车辆信息（车型、配置等） */
    private VehicleInfo vehicleInfo;

    /** 订单金额信息（指导价、成交价、优惠等） */
    private OrderAmount orderAmount;

    // ==================== 下单人信息（兼容字段） ====================

    /** 下单人ID，关联用户账号 */
    private String orderPersonId;

    /** 下单人类型：1-个人、2-企业 */
    private Integer orderPersonType;

    /** 下单人姓名 */
    private String orderPersonName;

    /** 下单人手机号 */
    private String orderPersonPhone;

    /** 下单人证件类型：1-身份证、2-护照等 */
    private Integer orderPersonIdType;

    /** 下单人证件号码 */
    private String orderPersonIdNum;

    /** 购车方案编码 */
    private Integer purchasePlan;

    // ==================== 车辆配置相关字段 ====================

    /** 销售车型编码 */
    private String saleModel;

    /** 生产配置代码，对应VMD系统的buildConfig */
    private String buildConfigCode;

    /** 归属区域编码 */
    private String regionCode;

    /** 门店编码 */
    private String storeCode;

    /** 销售顾问编码 */
    private String salesCode;

    /** 选配配置明细，key为配置项编码，value为配置详情 */
    private Map<String, OrderModelConfig> modelConfigMap;

    // ==================== 交付相关字段 ====================

    /** 上牌城市编码 */
    private String licenseCity;

    /** 经销商编码 */
    private String dealership;

    /** 交付中心编码 */
    private String deliveryCenter;

    /** 发运申请人ID */
    private String transportApplyPersonId;

    /** 发运申请人姓名 */
    private String transportApplyPersonName;

    /** 交付人员ID */
    private String deliveryPersonId;

    /** 交付人员姓名 */
    private String deliveryPersonName;

    /** 交付车辆VIN */
    private String deliveryVin;

    // ==================== 其他字段 ====================

    /** 备注 */
    private String remark;

    /** 有效标记，用于软删除 */
    private Boolean valid;

    /** 领域事件列表，用于事件驱动架构 */
    private List<OrderDomainEvent> domainEvents;

    /** 创建时间 */
    private Date createTime;

    /** 修改时间 */
    private Date modifyTime;

    // ==================== 私有方法 ====================

    /**
     * 生成订单号
     * 调用OrderNoGenerator生成唯一业务订单号
     */
    private void generateOrderNo() {
        this.orderNo = OrderNoGenerator.generate();
    }

    // ==================== 领域事件管理 ====================

    /**
     * 添加领域事件
     * 用于记录订单状态变更等重要业务事件
     *
     * @param event 领域事件对象
     */
    protected void addDomainEvent(OrderDomainEvent event) {
        if (this.domainEvents == null) {
            this.domainEvents = new ArrayList<>();
        }
        this.domainEvents.add(event);
    }

    /**
     * 获取并清除领域事件
     * 返回所有待处理的领域事件，并清空事件列表
     *
     * @return 领域事件列表
     */
    public List<OrderDomainEvent> getDomainEvents() {
        List<OrderDomainEvent> events = this.domainEvents == null ? new ArrayList<>() : new ArrayList<>(this.domainEvents);
        if (this.domainEvents != null) {
            this.domainEvents.clear();
        }
        return events;
    }

    // ==================== 下单业务方法 ====================

    /**
     * 意向金下单
     * 将订单转为意向金订单状态（小订单），生成订单号并设置初始状态为待支付意向金
     * 仅当订单类型为空或为SMALL时可调用
     */
    public void earnestMoneyOrder() {
        if (this.orderType == null || this.orderType == OrderType.SMALL) {
            this.orderType = OrderType.SMALL;
            generateOrderNo();
        }
        this.orderState = OrderState.EARNEST_MONEY_UNPAID;
        Date now = new Date();
        this.orderStateTime = now;
        this.orderTime = now;
    }

    /**
     * 定金下单
     * 将订单转为定金订单状态（正式订单），生成订单号并设置初始状态为待支付定金
     * 通常用于跳过意向金直接下定金的场景
     */
    public void downPaymentOrder() {
        if (this.orderNo == null || this.orderNo.isEmpty()) {
            this.orderType = OrderType.FORMAL;
            generateOrderNo();
        }
        this.orderState = OrderState.DOWN_PAYMENT_UNPAID;
        Date now = new Date();
        this.orderStateTime = now;
        this.orderTime = now;
    }

    // ==================== 信息保存方法 ====================

    /**
     * 保存生产配置
     * 设置订单的车辆生产配置，配置锁定后不可修改
     *
     * @param buildConfigCode 生产配置代码
     * @param modelConfigMap 选配配置明细
     * @throws SaleModelConfigHasLockedException 配置已锁定时抛出
     */
    public void saveBuildConfig(String buildConfigCode, Map<String, OrderModelConfig> modelConfigMap) {
        if (Boolean.TRUE.equals(buildConfigLock)) {
            throw new SaleModelConfigHasLockedException(orderNo);
        }
        this.buildConfigCode = buildConfigCode;
        this.modelConfigMap = modelConfigMap;
    }

    /**
     * 保存品牌编码
     */
    public void saveBrandCode(String brandCode) {
        this.brandCode = brandCode;
    }

    /**
     * 保存归属区域编码
     */
    public void saveRegionCode(String regionCode) {
        this.regionCode = regionCode;
    }

    /**
     * 保存销售车型编码
     */
    public void saveSaleModel(String saleModel) {
        this.saleModel = saleModel;
    }

    /**
     * 保存门店编码
     */
    public void saveStoreCode(String storeCode) {
        this.storeCode = storeCode;
    }

    /**
     * 保存销售顾问编码
     */
    public void saveSalesCode(String salesCode) {
        this.salesCode = salesCode;
    }

    /**
     * 保存下单人信息
     *
     * @param orderPersonId 下单人ID
     * @param orderPersonType 下单人类型
     * @param orderPersonName 下单人姓名
     * @param orderPersonIdType 证件类型
     * @param orderPersonIdNum 证件号码
     */
    public void saveOrderPerson(String orderPersonId, Integer orderPersonType, String orderPersonName,
                                 Integer orderPersonIdType, String orderPersonIdNum) {
        this.orderPersonId = orderPersonId;
        this.orderPersonType = orderPersonType;
        this.orderPersonName = orderPersonName;
        this.orderPersonIdType = orderPersonIdType;
        this.orderPersonIdNum = orderPersonIdNum;
    }

    /**
     * 保存购车方案
     */
    public void savePurchasePlan(Integer purchasePlan) {
        this.purchasePlan = purchasePlan;
    }

    /**
     * 保存上牌城市
     */
    public void saveLicenseCity(String licenseCity) {
        this.licenseCity = licenseCity;
    }

    /**
     * 保存客户类型
     */
    public void saveCustomerType(String customerType) {
        this.customerType = customerType;
    }

    /**
     * 保存付款方式
     */
    public void savePaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    /**
     * 保存经销商
     */
    public void saveDealership(String dealership) {
        this.dealership = dealership;
    }

    /**
     * 保存交付中心
     */
    public void saveDeliveryCenter(String deliveryCenter) {
        this.deliveryCenter = deliveryCenter;
    }

    /**
     * 保存交付人员
     */
    public void saveDeliveryPerson(String deliveryPersonId, String deliveryPersonName) {
        this.deliveryPersonId = deliveryPersonId;
        this.deliveryPersonName = deliveryPersonName;
    }

    /**
     * 保存交付车辆VIN
     */
    public void saveDeliveryVehicle(String deliveryVin) {
        this.deliveryVin = deliveryVin;
    }

    // ==================== 支付业务方法 ====================

    /**
     * 支付
     * 根据当前订单状态执行意向金或定金支付操作
     *
     * @param payAmount 支付金额
     * @throws OrderStateNotAllowedException 当前状态不允许支付时抛出
     */
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

    // ==================== 锁单业务方法 ====================

    /**
     * 锁单
     * 锁定生产配置，订单进入排产状态
     * 仅在定金已支付状态下可执行
     *
     * @throws OrderStateNotAllowedException 当前状态不允许锁单时抛出
     */
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

    // ==================== 取消/删除业务方法 ====================

    /**
     * 取消订单（无原因）
     * 将订单状态置为已取消
     */
    public void cancel() {
        this.orderState = OrderState.CANCEL;
        this.orderStateTime = new Date();
    }

    /**
     * 取消订单（带原因）
     * 将订单状态置为已取消，记录取消原因
     *
     * @param reason 取消原因
     */
    public void cancel(String reason) {
        this.orderState = OrderState.CANCEL;
        this.orderStateTime = new Date();
        this.remark = reason;
    }

    /**
     * 标记删除
     * 用于用户端删除订单，实际将订单置为取消状态
     */
    public void markDelete() {
        this.orderState = OrderState.CANCEL;
        this.orderStateTime = new Date();
    }

    /**
     * 管理端删除校验
     * 校验订单是否允许物理删除，仅已取消订单可删除
     *
     * @return 是否允许删除
     * @throws OrderIllegalDeleteException 订单状态不允许删除时抛出
     */
    public boolean manageDelete() {
        if (this.orderState != OrderState.CANCEL) {
            throw new OrderIllegalDeleteException(this.orderNo);
        }
        return true;
    }

    /**
     * 关闭订单
     * 将订单状态置为已关闭，记录关闭原因
     *
     * @param reason 关闭原因
     */
    public void close(String reason) {
        this.orderState = OrderState.CLOSED;
        this.orderStateTime = new Date();
        this.remark = reason;
    }

    /**
     * 完成订单
     * 将订单状态置为已完成，标记订单流程结束
     */
    public void complete() {
        this.orderState = OrderState.COMPLETED;
        this.orderStateTime = new Date();
    }

    // ==================== 退款业务方法 ====================

    /**
     * 申请退款
     * 将订单状态置为退款申请状态
     */
    public void requestRefund() {
        this.orderState = OrderState.REFUND_APPLY;
        this.orderStateTime = new Date();
    }

    // ==================== 意向金转定金 ====================

    /**
     * 意向金转定金
     * 将小订单转为正式订单，状态直接置为定金已支付
     * 用于意向金已支付后补交定金的场景
     */
    public void earnestMoneyToDownPayment() {
        this.orderType = OrderType.FORMAL;
        this.orderState = OrderState.DOWN_PAYMENT_PAID;
        Date now = new Date();
        this.orderStateTime = now;
        this.downPaymentTime = now;
    }

    // ==================== 发运/交付业务方法 ====================

    /**
     * 申请发运车辆
     * 记录发运申请人和申请时间
     *
     * @param transportApplyPersonId 发运申请人ID
     * @param transportApplyPersonName 发运申请人姓名
     */
    public void applyTransportVehicle(String transportApplyPersonId, String transportApplyPersonName) {
        this.transportApplyPersonId = transportApplyPersonId;
        this.transportApplyPersonName = transportApplyPersonName;
        this.transportApplyTime = new Date();
    }

    /**
     * 准备发运
     * 将订单状态置为准备发运
     */
    public void prepareTransport() {
        this.orderState = OrderState.PREPARE_TRANSPORT;
        this.orderStateTime = new Date();
    }

    /**
     * 发运中
     * 将订单状态置为发运中
     */
    public void transporting() {
        this.orderState = OrderState.TRANSPORTING;
        this.orderStateTime = new Date();
    }

    /**
     * 准备交付
     * 将订单状态置为准备交付
     */
    public void prepareDelivery() {
        this.orderState = OrderState.PREPARE_DELIVER;
        this.orderStateTime = new Date();
    }

    /**
     * 已交付
     * 将订单状态置为已交付，完成订单交付流程
     */
    public void delivered() {
        this.orderState = OrderState.DELIVERED;
        this.orderStateTime = new Date();
    }

    // ==================== 审核业务方法 ====================

    /**
     * 提交审核
     * 将订单状态置为待审核，等待审核人员审批
     */
    public void submit() {
        this.orderState = OrderState.PENDING_AUDIT;
        this.orderStateTime = new Date();
    }

    /**
     * 审核通过
     * 将订单状态置为审核通过，订单可继续后续流程
     */
    public void auditPass() {
        this.orderState = OrderState.AUDIT_PASSED;
        this.orderStateTime = new Date();
    }

    /**
     * 审核驳回
     * 将订单状态置为审核驳回，记录驳回原因
     *
     * @param reason 驳回原因
     */
    public void auditReject(String reason) {
        this.orderState = OrderState.AUDIT_REJECTED;
        this.orderStateTime = new Date();
        this.remark = reason;
    }

    // ==================== 值对象设置方法 ====================

    /**
     * 设置客户信息
     */
    public void setCustomerInfo(CustomerInfo customerInfo) {
        this.customerInfo = customerInfo;
    }

    /**
     * 设置车辆信息
     */
    public void setVehicleInfo(VehicleInfo vehicleInfo) {
        this.vehicleInfo = vehicleInfo;
    }

    /**
     * 设置组织归属信息
     */
    public void setOrganizationInfo(OrganizationInfo organizationInfo) {
        this.organizationInfo = organizationInfo;
    }

    /**
     * 设置订单金额
     */
    public void setOrderAmount(OrderAmount orderAmount) {
        this.orderAmount = orderAmount;
    }

    // ==================== 状态变更方法 ====================

    /**
     * 作废订单
     * 将订单状态置为已作废，通常用于超时未支付等场景
     */
    public void invalidate() {
        this.orderState = OrderState.EXPIRED;
        this.orderStateTime = new Date();
    }

    /**
     * 激活订单
     * 将订单状态置为已激活
     */
    public void activate() {
        this.orderState = OrderState.ACTIVATED;
        this.orderStateTime = new Date();
    }

    // ==================== 辅助方法 ====================

    /**
     * 获取订单状态值
     * 用于与数据库等外部系统交互
     *
     * @return 订单状态数值，null时返回null
     */
    public Integer getOrderStateValue() {
        return this.orderState != null ? this.orderState.getValue() : null;
    }

    // ==================== 静态工厂方法 ====================

    /**
     * 创建小订单（意向金订单）
     * 静态工厂方法，创建基础小订单对象
     *
     * @param orderId 订单ID
     * @param orderSource 订单来源
     * @return 小订单对象
     */
    public static Order createSmallOrder(String orderId, String orderSource) {
        Order order = new Order();
        order.id = orderId;
        order.orderSource = orderSource;
        return order;
    }

    /**
     * 创建正式订单
     * 静态工厂方法，创建基础正式订单对象
     *
     * @param orderId 订单ID
     * @param orderSource 订单来源
     * @return 正式订单对象
     */
    public static Order createFormalOrder(String orderId, String orderSource) {
        Order order = new Order();
        order.id = orderId;
        order.orderSource = orderSource;
        return order;
    }

    /**
     * 从心愿单创建订单
     * 静态工厂方法，将心愿单转为小订单
     *
     * @param accountId 用户账号ID
     * @param saleModel 销售车型编码
     * @return 心愿单转化的小订单对象
     */
    public static Order fromWishlist(String accountId, String saleModel) {
        Order order = new Order();
        order.id = IdUtil.nanoId(15);
        order.orderType = OrderType.SMALL;
        order.orderSource = "capp";
        order.customerType = CustomerType.PERSONAL.getCode();
        order.hasException = false;
        order.currentVersionNo = 1;
        order.lockedFlag = false;
        order.reopenFlag = false;
        order.orderPersonId = accountId;
        order.saleModel = saleModel;
        order.orderState = OrderState.WISHLIST;
        return order;
    }

}