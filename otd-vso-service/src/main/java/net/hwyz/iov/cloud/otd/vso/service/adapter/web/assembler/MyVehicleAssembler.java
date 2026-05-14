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
    @Mapping(target = "createTime", source = "createTime")
    @Mapping(target = "modifyTime", source = "modifyTime")
    @Mapping(target = "displayName", source = "displayName")
    @Mapping(target = "saleModelCode", source = "saleModelCode")
    @Mapping(target = "buildConfigCode", source = "buildConfigCode")
    @Mapping(target = "saleModelConfigType", source = "saleModelConfigType")
    @Mapping(target = "saleModelConfigName", source = "saleModelConfigName")
    @Mapping(target = "saleModelImages", source = "saleModelImages")
    @Mapping(target = "totalPrice", source = "totalPrice")
    @Mapping(target = "isValid", source = "isValid")
    MyVehicleVo fromWishlist(WishlistListResult wishlist);

    @Mapping(target = "id", source = "orderNo")
    @Mapping(target = "type", constant = "ORDER")
    @Mapping(target = "state", source = "orderState")
    @Mapping(target = "createTime", source = "createTime")
    @Mapping(target = "modifyTime", source = "modifyTime")
    @Mapping(target = "displayName", source = "displayName")
    @Mapping(target = "saleModelCode", source = "saleModel")
    @Mapping(target = "buildConfigCode", source = "buildConfigCode")
    @Mapping(target = "saleModelConfigType", source = "saleModelConfigType")
    @Mapping(target = "saleModelConfigName", source = "saleModelConfigName")
    @Mapping(target = "saleModelImages", source = "saleModelImages")
    @Mapping(target = "totalPrice", source = "totalPrice")
    @Mapping(target = "isValid", expression = "java(true)")
    MyVehicleVo fromOrder(OrderListResult order);

    List<MyVehicleVo> fromWishlistList(List<WishlistListResult> wishlists);

    List<MyVehicleVo> fromOrderList(List<OrderListResult> orders);

}