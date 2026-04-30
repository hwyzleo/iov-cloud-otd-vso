package net.hwyz.iov.cloud.otd.vso.service.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.otd.vso.api.enums.SaleModelConfigType;

import java.math.BigDecimal;

/**
 * 车辆销售订单车型配置领域对象
 *
 * @author hwyz_leo
 */
@Slf4j
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderModelConfig {

    /** 主键 ID */
    private String id;

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

}
