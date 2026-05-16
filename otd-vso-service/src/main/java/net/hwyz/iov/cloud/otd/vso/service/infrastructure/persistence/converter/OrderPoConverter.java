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
            @Mapping(target="orderNo", source = "orderNo"),
            @Mapping(target="id", source = "orderId"),
            @Mapping(target="orderType", source = "orderType"),
            @Mapping(target="orderSource", source = "orderSource"),
            @Mapping(target="brandCode", source = "brandCode"),
            @Mapping(target="buildConfigCode", source = "buildConfigCode"),
            @Mapping(target="saleModel", source = "saleModel"),
            @Mapping(target="regionCode", source = "regionCode"),
            @Mapping(target="storeCode", source = "storeCode"),
            @Mapping(target="salesCode", source = "salesCode"),
            @Mapping(target="licenseCity", source = "licenseCity"),
            @Mapping(target="orderState", expression = "java(orderPo.getOrderState() != null ? net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderState.fromValue(orderPo.getOrderState()) : null)"),
            @Mapping(target="hasException", expression = "java(orderPo.getHasException() != null && orderPo.getHasException() == 1)"),
            @Mapping(target="lockedFlag", expression = "java(orderPo.getLockedFlag() != null && orderPo.getLockedFlag() == 1)"),
            @Mapping(target="reopenFlag", expression = "java(orderPo.getReopenFlag() != null && orderPo.getReopenFlag() == 1)"),
            @Mapping(target="orderTime", source = "createdAtBusiness")
    })
    @org.mapstruct.BeanMapping(ignoreUnmappedSourceProperties = {"payState", "orderStateTime", "modelConfigType", "modelConfigName", "modelConfigPrice", "modelConfigMap", "modelConfigDesc", "totalPrice", "dealership", "deliveryCenter", "deliveryVin", "orderPersonId", "orderPersonType", "orderPersonName", "orderPersonIdType", "orderPersonIdNum", "purchasePlan", "transportApplyPersonId", "transportApplyPersonName", "deliveryPersonId", "deliveryPersonName", "earnestMoneyTime", "earnestMoneyAmount", "downPaymentTime", "downPaymentAmount", "transportApplyTime", "customerInfo", "organizationInfo", "vehicleInfo", "orderAmount", "orderPersonPhone", "buildConfigLock", "remark", "valid", "domainEvents", "createBy", "modifyBy"})
    Order toDomain(OrderPo orderPo);

    /**
     * 领域对象转数据对象
     *
     * @param order 领域对象
     * @return 数据对象
     */
    @Mappings({
            @Mapping(target="id", ignore = true),
            @Mapping(target="orderNo", source = "orderNo"),
            @Mapping(target="orderId", source = "id"),
            @Mapping(target="orderType", source = "orderType"),
            @Mapping(target="orderSource", source = "orderSource"),
            @Mapping(target="brandCode", source = "brandCode"),
            @Mapping(target="buildConfigCode", source = "buildConfigCode"),
            @Mapping(target="saleModel", source = "saleModel"),
            @Mapping(target="regionCode", source = "regionCode"),
            @Mapping(target="storeCode", source = "storeCode"),
            @Mapping(target="salesCode", source = "salesCode"),
            @Mapping(target="licenseCity", source = "licenseCity"),
            @Mapping(target="orderState", expression = "java(order.getOrderState() != null ? order.getOrderState().getValue() : null)"),
            @Mapping(target="hasException", expression = "java(order.getHasException() != null && order.getHasException() ? 1 : 0)"),
            @Mapping(target="lockedFlag", expression = "java(order.getLockedFlag() != null && order.getLockedFlag() ? 1 : 0)"),
            @Mapping(target="reopenFlag", expression = "java(order.getReopenFlag() != null && order.getReopenFlag() ? 1 : 0)"),
            @Mapping(target="createdAtBusiness", source = "orderTime")
    })
    @org.mapstruct.BeanMapping(ignoreUnmappedSourceProperties = {"earnestMoneyTime", "earnestMoneyAmount", "downPaymentTime", "downPaymentAmount", "transportApplyTime", "customerInfo", "organizationInfo", "vehicleInfo", "orderAmount", "orderPersonPhone", "buildConfigLock", "remark", "valid", "domainEvents", "payState", "orderState", "orderStateTime", "modelConfigType", "modelConfigName", "modelConfigPrice", "modelConfigMap", "modelConfigDesc", "totalPrice", "dealership", "deliveryCenter", "deliveryVin", "orderPersonId", "orderPersonType", "orderPersonName", "orderPersonIdType", "orderPersonIdNum", "purchasePlan", "transportApplyPersonId", "transportApplyPersonName", "deliveryPersonId", "deliveryPersonName", "sourceRemark", "endType", "vehicleVin", "cancelReason", "closeReason", "voidReason", "auditSubmitTime", "auditPassTime", "deliveryFinishTime", "finishTime", "cancelTime", "closeTime", "createTime", "createBy", "modifyTime", "modifyBy"})
    OrderPo fromDomain(Order order);

}
