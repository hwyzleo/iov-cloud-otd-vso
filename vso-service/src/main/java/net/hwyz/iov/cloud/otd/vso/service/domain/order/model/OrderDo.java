package net.hwyz.iov.cloud.otd.vso.service.domain.order.model;

import cn.hutool.core.util.IdUtil;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.otd.vso.api.contract.enums.SaleModelConfigType;
import net.hwyz.iov.cloud.otd.vso.service.domain.contract.enums.OrderState;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.exception.OrderIllegalDeleteException;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.exception.OrderNotExistException;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.exception.OrderStateNotAllowedException;
import net.hwyz.iov.cloud.tsp.framework.commons.domain.BaseDo;
import net.hwyz.iov.cloud.tsp.framework.commons.domain.DomainObj;

import java.math.BigDecimal;
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
public class OrderDo extends BaseDo<String> implements DomainObj<OrderDo> {

    /**
     * 订单编码
     */
    private String orderNum;
    /**
     * 订单状态
     */
    private OrderState orderState;
    /**
     * 下单人员ID
     */
    private String orderPersonId;
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
     * 初始化
     *
     * @param orderPersonId 下单人ID
     * @param saleCode      销售代码
     */
    public void init(String orderPersonId, String saleCode, OrderState orderState) {
        generateOrderNum();
        this.orderState = orderState;
        this.modelConfigLock = false;
        this.orderPersonId = orderPersonId;
        this.saleCode = saleCode;
        stateInit();
    }

    /**
     * 保存车型配置
     *
     * @param modelConfigCode 车型配置代码
     */
    public void saveModelConfig(String modelConfigCode, Map<SaleModelConfigType, OrderModelConfigDo> modelConfigMap) {
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
        modelConfigMap.values().forEach(modelConfig -> {
            if (desc.length() > 0) {
                desc.append(" | ");
            }
            desc.append(modelConfig.getTypeName());
        });
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
        stateChange();
    }

    /**
     * 支付订单
     */
    public void pay() {
        switch (this.orderState) {
            case EARNEST_MONEY_UNPAID -> {
                this.orderState = OrderState.EARNEST_MONEY_PAID;
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
        stateChange();
    }

    /**
     * 生成订单编码
     */
    private void generateOrderNum() {
        this.orderNum = IdUtil.nanoId(15);
    }

}
