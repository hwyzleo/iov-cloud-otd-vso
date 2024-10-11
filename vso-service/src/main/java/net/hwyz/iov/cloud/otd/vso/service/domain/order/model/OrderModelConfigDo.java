package net.hwyz.iov.cloud.otd.vso.service.domain.order.model;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.otd.vso.api.contract.enums.SaleModelConfigType;
import net.hwyz.iov.cloud.tsp.framework.commons.domain.BaseDo;
import net.hwyz.iov.cloud.tsp.framework.commons.domain.DomainObj;

import java.math.BigDecimal;

/**
 * 车辆销售订单车型配置领域对象
 *
 * @author hwyz_leo
 */
@Slf4j
@Getter
@SuperBuilder
public class OrderModelConfigDo extends BaseDo<Long> implements DomainObj<OrderDo> {

    /**
     * 销售车型配置类型
     */
    private SaleModelConfigType type;
    /**
     * 销售车型配置类型代码
     */
    private String typeCode;
    /**
     * 销售车型配置类型名称
     */
    private String typeName;
    /**
     * 销售车型配置类型价格
     */
    private BigDecimal typePrice;

    /**
     * 初始化
     */
    public void init() {
        stateInit();
    }

    /**
     * 标记删除
     */
    public void markDelete() {
        stateDelete();
    }

}
