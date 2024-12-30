package net.hwyz.iov.cloud.otd.vso.api.feign.mpt;

import net.hwyz.iov.cloud.framework.common.bean.MptAccount;
import net.hwyz.iov.cloud.framework.common.web.page.TableDataInfo;
import net.hwyz.iov.cloud.otd.vso.api.contract.VehicleSaleOrderMpt;
import net.hwyz.iov.cloud.otd.vso.api.contract.request.AssignDeliveryPersonRequest;
import net.hwyz.iov.cloud.otd.vso.api.contract.request.AssignVehicleRequest;

/**
 * 车辆销售订单相关管理接口
 *
 * @author hwyz_leo
 */
public interface VehicleSaleOrderMptApi {

    /**
     * 分页查询车辆销售订单信息
     *
     * @param vehicleSaleOrder 车辆销售订单信息
     * @return 车辆销售订单信息列表
     */
    TableDataInfo list(VehicleSaleOrderMpt vehicleSaleOrder);

    /**
     * 分页查询可改配车辆销售订单信息
     *
     * @param vehicleSaleOrder 车辆销售订单信息
     * @return 车辆销售订单信息列表
     */
    TableDataInfo listModelConfigChangeable(VehicleSaleOrderMpt vehicleSaleOrder);

    /**
     * 分页查询没有交付人员的车辆销售订单信息
     *
     * @param vehicleSaleOrder 车辆销售订单信息
     * @return 车辆销售订单信息列表
     */
    TableDataInfo listWithoutDeliveryPerson(VehicleSaleOrderMpt vehicleSaleOrder);

    /**
     * 分页查询可配车车辆销售订单信息
     *
     * @param vehicleSaleOrder 车辆销售订单信息
     * @return 车辆销售订单信息列表
     */
    TableDataInfo listAssignable(VehicleSaleOrderMpt vehicleSaleOrder);

    /**
     * 分配交付人员
     *
     * @param request    分配交付人员请求
     * @param mptAccount 管理后台用户
     */
    void assignDeliveryPerson(AssignDeliveryPersonRequest request, MptAccount mptAccount);

    /**
     * 分配车辆
     *
     * @param request 分配车辆请求
     */
    void assignVehicle(AssignVehicleRequest request);

}
