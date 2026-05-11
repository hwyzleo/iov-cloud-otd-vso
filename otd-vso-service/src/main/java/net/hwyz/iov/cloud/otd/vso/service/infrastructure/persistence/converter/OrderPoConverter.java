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
            @Mapping(target="id", source = "orderId")
    })
    @org.mapstruct.BeanMapping(ignoreUnmappedSourceProperties = {"orderNo", "payState", "orderState", "orderStateTime", "orderTime", "saleCode", "modelConfigType", "modelConfigName", "modelConfigPrice", "modelConfigMap", "modelConfigDesc", "totalPrice", "licenseCity", "dealership", "deliveryCenter", "deliveryVin", "orderPersonId", "orderPersonType", "orderPersonName", "orderPersonIdType", "orderPersonIdNum", "purchasePlan", "transportApplyPersonId", "transportApplyPersonName", "deliveryPersonId", "deliveryPersonName", "earnestMoneyTime", "earnestMoneyAmount", "downPaymentTime", "downPaymentAmount", "transportApplyTime", "customerInfo", "organizationInfo", "vehicleInfo", "orderAmount", "orderPersonPhone", "buildConfigLock", "remark", "valid", "domainEvents"})
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
            @Mapping(target="orderId", source = "id")
    })
    @org.mapstruct.BeanMapping(ignoreUnmappedSourceProperties = {"orderNo", "earnestMoneyTime", "earnestMoneyAmount", "downPaymentTime", "downPaymentAmount", "transportApplyTime", "customerInfo", "organizationInfo", "vehicleInfo", "orderAmount", "orderPersonPhone", "buildConfigLock", "remark", "valid", "domainEvents", "payState", "orderState", "orderStateTime", "orderTime", "saleCode", "modelConfigType", "modelConfigName", "modelConfigPrice", "modelConfigMap", "modelConfigDesc", "totalPrice", "licenseCity", "dealership", "deliveryCenter", "deliveryVin", "orderPersonId", "orderPersonType", "orderPersonName", "orderPersonIdType", "orderPersonIdNum", "purchasePlan", "transportApplyPersonId", "transportApplyPersonName", "deliveryPersonId", "deliveryPersonName", "sourceRemark", "customerType", "mainStatus", "endType", "previousMainStatus", "brandCode", "regionCode", "storeCode", "salesCode", "vehicleVin", "hasException", "currentVersionNo", "lockedFlag", "reopenFlag", "cancelReason", "closeReason", "voidReason", "createdAtBusiness", "auditSubmitTime", "auditPassTime", "deliveryFinishTime", "finishTime", "cancelTime", "closeTime", "createTime", "createBy", "modifyTime", "modifyBy"})
    OrderPo fromDomain(Order order);

}
