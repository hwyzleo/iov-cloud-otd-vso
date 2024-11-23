package net.hwyz.iov.cloud.otd.vso.api.feign.mpt;

import net.hwyz.iov.cloud.otd.vso.api.contract.request.AssignDeliveryPersonRequest;
import net.hwyz.iov.cloud.otd.vso.api.contract.request.AssignVehicleRequest;
import net.hwyz.iov.cloud.tsp.framework.commons.bean.MptAccount;

/**
 * 车辆销售订单相关管理接口
 *
 * @author hwyz_leo
 */
public interface VehicleSaleOrderMptApi {

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
     * @param request    分配车辆请求
     * @param mptAccount 管理后台用户
     */
    void assignVehicle(AssignVehicleRequest request, MptAccount mptAccount);

}