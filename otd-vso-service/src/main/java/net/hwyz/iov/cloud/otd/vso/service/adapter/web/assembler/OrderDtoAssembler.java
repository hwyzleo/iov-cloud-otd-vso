package net.hwyz.iov.cloud.otd.vso.service.adapter.web.assembler;

import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.*;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * Order DTO 转换器
 *
 * @author VSO Team
 */
@Mapper
public interface OrderDtoAssembler {

    OrderDtoAssembler INSTANCE = Mappers.getMapper(OrderDtoAssembler.class);

    @Mapping(target = "orderNum", source = "orderNo")
    @Mapping(target = "orderState", expression = "java(order.getOrderState() != null ? order.getOrderState().getValue() : null)")
    @Mapping(target = "displayName", expression = "java(order.getDeliveryVin() != null ? order.getDeliveryVin() : order.getBuildConfigCode())")
    OrderListResult toOrderListResult(Order order);

    List<OrderListResult> toOrderListResultList(List<Order> orders);

    @Mapping(target = "orderNum", source = "orderNo")
    @Mapping(target = "orderState", expression = "java(order.getOrderState() != null ? order.getOrderState().getValue() : null)")
    @Mapping(target = "licenseCityCode", source = "licenseCity")
    @Mapping(target = "dealershipCode", source = "dealership")
    @Mapping(target = "deliveryCenterCode", source = "deliveryCenter")
    OrderDetailResult toOrderDetailResult(Order order);

    @Mapping(target = "orderNum", source = "orderNo")
    WishlistDetailResult toWishlistDetailResult(Order order);

}
