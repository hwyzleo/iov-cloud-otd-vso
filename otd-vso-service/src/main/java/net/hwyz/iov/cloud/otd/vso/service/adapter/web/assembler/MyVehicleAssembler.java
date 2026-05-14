package net.hwyz.iov.cloud.otd.vso.service.adapter.web.assembler;

import net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo.MyVehicleVo;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.OrderListResult;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.WishlistListResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 我的车辆 Assembler（合并心愿单和订单）
 *
 * @author VSO Team
 */
@Mapper
public interface MyVehicleAssembler {

    MyVehicleAssembler INSTANCE = Mappers.getMapper(MyVehicleAssembler.class);

    @Mapping(target = "id", source = "wishlistId")
    @Mapping(target = "type", constant = "WISHLIST")
    @Mapping(target = "state", expression = "java(100)")
    @Mapping(target = "displayName", source = "displayName")
    @Mapping(target = "saleModelImages", source = "saleModelImages")
    @Mapping(target = "totalPrice", source = "totalPrice")
    @Mapping(target = "saleModelDesc", source = "saleModelDesc")
    MyVehicleVo fromWishlist(WishlistListResult wishlist);

    @Mapping(target = "id", source = "orderNo")
    @Mapping(target = "type", constant = "ORDER")
    @Mapping(target = "state", source = "orderState")
    @Mapping(target = "displayName", source = "displayName")
    @Mapping(target = "saleModelImages", source = "saleModelImages")
    @Mapping(target = "totalPrice", source = "totalPrice")
    @Mapping(target = "saleModelDesc", source = "saleModelDesc")
    MyVehicleVo fromOrder(OrderListResult order);

    List<MyVehicleVo> fromWishlistList(List<WishlistListResult> wishlists);

    List<MyVehicleVo> fromOrderList(List<OrderListResult> orders);

}