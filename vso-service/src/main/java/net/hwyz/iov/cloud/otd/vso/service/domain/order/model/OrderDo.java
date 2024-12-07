package net.hwyz.iov.cloud.otd.vso.service.domain.order.model;

import cn.hutool.core.util.IdUtil;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.framework.common.domain.BaseDo;
import net.hwyz.iov.cloud.framework.common.domain.DomainObj;
import net.hwyz.iov.cloud.otd.vso.api.contract.enums.SaleModelConfigType;
import net.hwyz.iov.cloud.otd.vso.service.domain.contract.enums.OrderState;
import net.hwyz.iov.cloud.otd.vso.service.domain.contract.enums.PayState;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.exception.OrderIllegalDeleteException;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.exception.OrderNotExistException;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.exception.OrderStateNotAllowedException;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.exception.SaleModelConfigHasLockedException;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 车辆销售订单领域对象
 *
 * @author hwyz_leo
 */
@Slf4j
@Getter
@SuperBuilder
public class OrderDo extends BaseDo<Long> implements DomainObj<OrderDo> {

    /**
     * 订单编码
     */
    private String orderNum;
    /**
     * 订单状态
     */
    private OrderState orderState;
    /**
     * 订单状态时间
     */
    private Date orderStateTime;
    /**
     * 下单时间
     */
    private Date orderTime;
    /**
     * 支付状态
     */
    private PayState payState;
    /**
     * 意向金支付时间
     */
    private Date earnestMoneyTime;
    /**
     * 意向金支付金额
     */
    private BigDecimal earnestMoneyAmount;
    /**
     * 定金支付时间
     */
    private Date downPaymentTime;
    /**
     * 定金支付金额
     */
    private BigDecimal downPaymentAmount;
    /**
     * 锁单时间
     */
    private Date lockTime;
    /**
     * 下单人员ID
     */
    private String orderPersonId;
    /**
     * 下单人员类型
     */
    private Integer orderPersonType;
    /**
     * 下单人员姓名
     */
    private String orderPersonName;
    /**
     * 下单人员电话
     */
    private String orderPersonPhone;
    /**
     * 下单人员证件类型
     */
    private Integer orderPersonIdType;
    /**
     * 下单人员证件号码
     */
    private String orderPersonIdNum;
    /**
     * 购车方案
     */
    private Integer purchasePlan;
    /**
     * 销售代码
     */
    private String saleCode;
    /**
     * 车型配置代码
     */
    private String modelConfigCode;
    /**
     * 车型配置是否锁定
     */
    private Boolean modelConfigLock;
    /**
     * 订单车型配置类型Map
     */
    private Map<SaleModelConfigType, OrderModelConfigDo> modelConfigMap;
    /**
     * 上牌城市
     */
    private String licenseCity;
    /**
     * 销售门店
     */
    private String dealership;
    /**
     * 交付中心
     */
    private String deliveryCenter;
    /**
     * 交付人员ID
     */
    private String deliveryPersonId;
    /**
     * 交付人员姓名
     */
    private String deliveryPersonName;
    /**
     * 交付车辆
     */
    private String deliveryVin;

    /**
     * 初始化
     *
     * @param orderPersonId    下单人ID
     * @param orderPersonPhone 下单人电话
     * @param saleCode         销售代码
     */
    public void init(String orderPersonId, String orderPersonPhone, String saleCode, OrderState orderState) {
        generateOrderNum();
        this.orderState = orderState;
        Date now = new Date();
        this.orderStateTime = now;
        if (this.orderState != OrderState.WISHLIST) {
            this.orderTime = now;
        }
        this.modelConfigLock = false;
        this.orderPersonId = orderPersonId;
        this.orderPersonPhone = orderPersonPhone;
        this.saleCode = saleCode;
        stateInit();
    }

    /**
     * 意向金下单
     */
    public void earnestMoneyOrder() {
        this.orderState = OrderState.EARNEST_MONEY_UNPAID;
        Date now = new Date();
        this.orderStateTime = now;
        this.orderTime = now;
        stateChange();
    }

    /**
     * 定金下单
     */
    public void downPaymentOrder() {
        this.orderState = OrderState.DOWN_PAYMENT_UNPAID;
        Date now = new Date();
        this.orderStateTime = now;
        this.orderTime = now;
        stateChange();
    }

    /**
     * 保存车型配置
     *
     * @param modelConfigCode 车型配置代码
     */
    public void saveModelConfig(String modelConfigCode, Map<SaleModelConfigType, OrderModelConfigDo> modelConfigMap) {
        if (modelConfigLock) {
            throw new SaleModelConfigHasLockedException(orderNum);
        }
        if (this.modelConfigCode == null || !this.modelConfigCode.equals(modelConfigCode)) {
            this.modelConfigCode = modelConfigCode;
            this.modelConfigMap = modelConfigMap;
            stateChange();
        }
    }

    /**
     * 获取车型配置类型
     *
     * @return 车型配置类型Map
     */
    public Map<String, String> getModelConfigType() {
        return modelConfigMap.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().name(), e -> e.getValue().getTypeCode()));
    }

    /**
     * 获取车型配置名称
     *
     * @return 车型配置名称Map
     */
    public Map<String, String> getModelConfigName() {
        return modelConfigMap.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().name(), e -> e.getValue().getTypeName()));
    }

    /**
     * 获取车型配置描述
     *
     * @return 车型配置描述
     */
    public String getModelConfigDesc() {
        StringBuilder desc = new StringBuilder();
        if (modelConfigMap.containsKey(SaleModelConfigType.MODEL)) {
            desc.append(modelConfigMap.get(SaleModelConfigType.MODEL).getTypeName());
            desc.append(" | ");
        }
        if (modelConfigMap.containsKey(SaleModelConfigType.SPARE_TIRE)) {
            desc.append(modelConfigMap.get(SaleModelConfigType.SPARE_TIRE).getTypeName());
            desc.append(" | ");
        }
        if (modelConfigMap.containsKey(SaleModelConfigType.EXTERIOR)) {
            desc.append(modelConfigMap.get(SaleModelConfigType.EXTERIOR).getTypeName());
            desc.append(" | ");
        }
        if (modelConfigMap.containsKey(SaleModelConfigType.WHEEL)) {
            desc.append(modelConfigMap.get(SaleModelConfigType.WHEEL).getTypeName());
            desc.append(" | ");
        }
        if (modelConfigMap.containsKey(SaleModelConfigType.INTERIOR)) {
            desc.append(modelConfigMap.get(SaleModelConfigType.INTERIOR).getTypeName());
            desc.append(" | ");
        }
        if (modelConfigMap.containsKey(SaleModelConfigType.ADAS)) {
            desc.append(modelConfigMap.get(SaleModelConfigType.ADAS).getTypeName());
        }
        return desc.toString();
    }

    /**
     * 获取车型配置价格
     *
     * @return 车型配置价格Map
     */
    public Map<String, BigDecimal> getModelConfigPrice() {
        return modelConfigMap.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().name(), e -> e.getValue().getTypePrice()));
    }

    /**
     * 获取车型配置总价
     *
     * @return 车型配置总价
     */
    public BigDecimal getTotalPrice() {
        return modelConfigMap.values().stream()
                .map(OrderModelConfigDo::getTypePrice).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 保存购车方案
     *
     * @param purchasePlan 购车方案
     */
    public void savePurchasePlan(Integer purchasePlan) {
        if (this.purchasePlan == null || !this.purchasePlan.equals(purchasePlan)) {
            this.purchasePlan = purchasePlan;
            stateChange();
        }
    }

    /**
     * 保存下单人信息
     *
     * @param orderPersonId     下单人ID
     * @param orderPersonType   下单人类型
     * @param orderPersonName   下单人姓名
     * @param orderPersonIdType 下单人证件类型
     * @param orderPersonIdNum  下单人证件号
     */
    public void saveOrderPerson(String orderPersonId, Integer orderPersonType, String orderPersonName,
                                Integer orderPersonIdType, String orderPersonIdNum) {
        if (this.orderPersonId == null) {
            this.orderPersonId = orderPersonId;
            stateChange();
        }
        if (this.orderPersonType == null || !this.orderPersonType.equals(orderPersonType)) {
            this.orderPersonType = orderPersonType;
            stateChange();
        }
        if (this.orderPersonName == null || !this.orderPersonName.equals(orderPersonName)) {
            this.orderPersonName = orderPersonName;
            stateChange();
        }
        if (this.orderPersonIdType == null || !this.orderPersonIdType.equals(orderPersonIdType)) {
            this.orderPersonIdType = orderPersonIdType;
            stateChange();
        }
        if (this.orderPersonIdNum == null || !this.orderPersonIdNum.equals(orderPersonIdNum)) {
            this.orderPersonIdNum = orderPersonIdNum;
            stateChange();
        }
    }

    /**
     * 保存上牌城市
     *
     * @param licenseCity 上牌城市
     */
    public void saveLicenseCity(String licenseCity) {
        if (this.licenseCity == null || !this.licenseCity.equals(licenseCity)) {
            this.licenseCity = licenseCity;
            stateChange();
        }
    }

    /**
     * 保存销售门店
     *
     * @param dealership 销售门店
     */
    public void saveDealership(String dealership) {
        if (this.dealership == null || !this.dealership.equals(dealership)) {
            this.dealership = dealership;
            stateChange();
        }
    }

    /**
     * 保存交付中心
     *
     * @param deliveryCenter 交付中心
     */
    public void saveDeliveryCenter(String deliveryCenter) {
        if (this.deliveryCenter == null || !this.deliveryCenter.equals(deliveryCenter)) {
            this.deliveryCenter = deliveryCenter;
            stateChange();
        }
    }

    /**
     * 标记删除
     */
    public void markDelete() {
        if (this.orderState != OrderState.WISHLIST) {
            throw new OrderIllegalDeleteException(this.orderNum);
        }
        if (this.modelConfigMap != null) {
            this.modelConfigMap.values().forEach(OrderModelConfigDo::markDelete);
        }
        stateDelete();
    }

    /**
     * 取消订单
     */
    public void cancel() {
        if (this.orderState == OrderState.WISHLIST) {
            throw new OrderNotExistException(this.orderNum);
        }
        this.orderState = OrderState.CANCEL;
        this.orderStateTime = new Date();
        stateChange();
    }

    /**
     * 支付订单
     *
     * @param payAmount 支付金额
     */
    public void pay(BigDecimal payAmount) {
        switch (this.orderState) {
            case EARNEST_MONEY_UNPAID -> {
                this.orderState = OrderState.EARNEST_MONEY_PAID;
                Date now = new Date();
                this.orderStateTime = now;
                this.earnestMoneyTime = now;
                this.earnestMoneyAmount = payAmount;
                this.payState = PayState.EARNEST_MONEY_PAID;
                stateChange();
            }
            case DOWN_PAYMENT_UNPAID -> {
                this.orderState = OrderState.DOWN_PAYMENT_PAID;
                Date now = new Date();
                this.orderStateTime = now;
                this.downPaymentTime = now;
                this.downPaymentAmount = payAmount;
                this.payState = PayState.DOWN_PAYMENT_PAID;
                stateChange();
            }
            default -> throw new OrderStateNotAllowedException(this.orderNum, this.orderState, "PAY");
        }
    }

    /**
     * 申请退款订单
     */
    public void requestRefund() {
        if (this.orderState.value < OrderState.EARNEST_MONEY_PAID.value) {
            throw new OrderStateNotAllowedException(this.orderNum, this.orderState, "REQUEST_REFUND");
        }
        this.orderState = OrderState.REFUND_APPLY;
        this.orderStateTime = new Date();
        stateChange();
    }

    /**
     * 意向金转定金
     */
    public void earnestMoneyToDownPayment() {
        if (this.orderState != OrderState.EARNEST_MONEY_PAID) {
            throw new OrderStateNotAllowedException(this.orderNum, this.orderState, "EARNEST_MONEY_TO_DOWN_PAYMENT");
        }
        this.orderState = OrderState.DOWN_PAYMENT_PAID;
        Date now = new Date();
        this.orderStateTime = now;
        this.downPaymentTime = now;
        this.downPaymentAmount = BigDecimal.ZERO;
        this.payState = PayState.DOWN_PAYMENT_PAID;
        stateChange();
    }

    /**
     * 锁定订单
     */
    public void lock() {
        if (this.orderState != OrderState.DOWN_PAYMENT_PAID) {
            throw new OrderStateNotAllowedException(this.orderNum, this.orderState, "LOCK");
        }
        this.modelConfigLock = true;
        this.orderState = OrderState.ARRANGE_PRODUCTION;
        Date now = new Date();
        this.orderStateTime = now;
        this.lockTime = now;
        stateChange();
    }

    /**
     * 保存交付人员信息
     *
     * @param deliveryPersonId   交付人员ID
     * @param deliveryPersonName 交付人员姓名
     */
    public void saveDeliveryPerson(String deliveryPersonId, String deliveryPersonName) {
        if (this.deliveryPersonId == null || !this.deliveryPersonId.equals(deliveryPersonId)) {
            this.deliveryPersonId = deliveryPersonId;
            stateChange();
        }
        if (this.deliveryPersonName == null || !this.deliveryPersonName.equals(deliveryPersonName)) {
            this.deliveryPersonName = deliveryPersonName;
            stateChange();
        }
    }

    /**
     * 保存交付车辆
     *
     * @param deliveryVin 交付车辆
     */
    public void saveDeliveryVehicle(String deliveryVin) {
        if (this.orderState != OrderState.ARRANGE_PRODUCTION) {
            throw new OrderStateNotAllowedException(this.orderNum, this.orderState, "DELIVERY_VEHICLE");
        }
        if (this.deliveryVin == null || !this.deliveryVin.equals(deliveryVin)) {
            this.deliveryVin = deliveryVin;
            this.orderState = OrderState.ALLOCATION_VEHICLE;
            this.orderStateTime = new Date();
            stateChange();
        }
    }

    /**
     * 准备运输
     */
    public void prepareTransport() {
        if (this.orderState != OrderState.ALLOCATION_VEHICLE) {
            throw new OrderStateNotAllowedException(this.orderNum, this.orderState, "PREPARE_TRANSPORT");
        }
        this.orderState = OrderState.PREPARE_TRANSPORT;
        this.orderStateTime = new Date();
        stateChange();
    }

    /**
     * 运输中
     */
    public void transporting() {
        if (this.orderState != OrderState.PREPARE_TRANSPORT) {
            throw new OrderStateNotAllowedException(this.orderNum, this.orderState, "TRANSPORTING");
        }
        this.orderState = OrderState.TRANSPORTING;
        this.orderStateTime = new Date();
        stateChange();
    }

    /**
     * 准备交付
     */
    public void prepareDelivery() {
        if (this.orderState != OrderState.TRANSPORTING) {
            throw new OrderStateNotAllowedException(this.orderNum, this.orderState, "PREPARE_DELIVER");
        }
        this.orderState = OrderState.PREPARE_DELIVER;
        this.orderStateTime = new Date();
        stateChange();
    }

    /**
     * 完成交付
     */
    public void delivered() {
        if (this.orderState != OrderState.PREPARE_DELIVER) {
            throw new OrderStateNotAllowedException(this.orderNum, this.orderState, "DELIVERED");
        }
        this.orderState = OrderState.DELIVERED;
        this.orderStateTime = new Date();
        stateChange();
    }

    /**
     * 激活车辆
     */
    public void activate() {
        if (this.orderState != OrderState.DELIVERED) {
            throw new OrderStateNotAllowedException(this.orderNum, this.orderState, "ACTIVATE");
        }
        this.orderState = OrderState.ACTIVATED;
        this.orderStateTime = new Date();
        stateChange();
    }

    /**
     * 生成订单编码
     */
    private void generateOrderNum() {
        this.orderNum = IdUtil.nanoId(15);
    }

}
