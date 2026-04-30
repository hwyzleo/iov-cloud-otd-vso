package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.converter;

import net.hwyz.iov.cloud.otd.vso.service.domain.model.Order;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderPo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

/**
 * 车辆销售订单数据对象转换类
 *
 * @author hwyz_leo
 */
@Mapper
public interface OrderPoConverter {

    OrderPoConverter INSTANCE = Mappers.getMapper(OrderPoConverter.class);

    /**
     * 数据对象转领域对象
     *
     * @param orderPo 数据对象
     * @return 领域对象
     */
    @Mappings({
            @Mapping(target="orderState", expression = "java(net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderState.valOf(orderPo.getOrderState()))"),
            @Mapping(target="orderNo", source = "orderNo"),
            @Mapping(target="id", source = "orderId"),
            @Mapping(target="modelConfigMap", ignore = true),
            @Mapping(target="payState", ignore = true)
    })
    @org.mapstruct.BeanMapping(ignoreUnmappedSourceProperties = {"orderNum", "payState", "earnestMoneyTime", "earnestMoneyAmount", "downPaymentTime", "downPaymentAmount", "transportApplyTime", "customerInfo", "organizationInfo", "vehicleInfo", "orderAmount", "orderPersonPhone", "buildConfigLock", "remark", "valid", "domainEvents"})
    Order toDomain(OrderPo orderPo);

    /**
     * 领域对象转数据对象
     *
     * @param order 领域对象
     * @return 数据对象
     */
    @Mappings({
            @Mapping(target="orderState", source = "orderState.value"),
            @Mapping(target="orderNo", source = "orderNo"),
            @Mapping(target="orderId", source = "id"),
            @Mapping(target="modelConfigMap", ignore = true)
    })
    @org.mapstruct.BeanMapping(ignoreUnmappedSourceProperties = {"orderNum", "earnestMoneyTime", "earnestMoneyAmount", "downPaymentTime", "downPaymentAmount", "transportApplyTime", "customerInfo", "organizationInfo", "vehicleInfo", "orderAmount", "orderPersonPhone", "buildConfigLock", "remark", "valid", "domainEvents", "payState", "modelConfigMap", "sourceRemark", "customerType", "mainStatus", "endType", "previousMainStatus", "brandCode", "regionCode", "storeCode", "salesCode", "vehicleVin", "hasException", "currentVersionNo", "lockedFlag", "reopenFlag", "cancelReason", "closeReason", "voidReason", "createdAtBusiness", "auditSubmitTime", "auditPassTime", "deliveryFinishTime", "finishTime", "cancelTime", "closeTime", "createTime", "createBy", "modifyTime", "modifyBy", "rowVersion", "rowValid", "modelConfigType", "modelConfigName", "modelConfigPrice", "modelConfigDesc", "totalPrice"})
    OrderPo fromDomain(Order order);

}
