package net.hwyz.iov.cloud.otd.vso.api.feign.mpt;

import net.hwyz.iov.cloud.framework.common.bean.MptAccount;
import net.hwyz.iov.cloud.framework.common.web.page.TableDataInfo;
import net.hwyz.iov.cloud.otd.vso.api.contract.DeliveryCenterStaffMpt;
import net.hwyz.iov.cloud.otd.vso.api.contract.TransportOrderMpt;
import net.hwyz.iov.cloud.otd.vso.api.contract.VehicleSaleOrderMpt;
import net.hwyz.iov.cloud.otd.vso.api.contract.request.ApplyTransportRequest;
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
     * 分页查询交付中心人员
     *
     * @param deliveryCenterStaff 交付中心人员信息
     * @return 交付中心人员信息列表
     */
    TableDataInfo listDeliveryCenterStaff(DeliveryCenterStaffMpt deliveryCenterStaff);

    /**
     * 分页查询可配车车辆销售订单信息
     *
     * @param vehicleSaleOrder 车辆销售订单信息
     * @return 车辆销售订单信息列表
     */
    TableDataInfo listAssignable(VehicleSaleOrderMpt vehicleSaleOrder);

    /**
     * 分页查询运输相关车辆销售订单信息
     *
     * @param transportOrder 车辆销售订单信息
     * @return 车辆销售订单信息列表
     */
    TableDataInfo listTransport(TransportOrderMpt transportOrder);

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

    /**
     * 申请发运
     *
     * @param request 申请发运请求
     */
    void applyTransport(ApplyTransportRequest request);

}
