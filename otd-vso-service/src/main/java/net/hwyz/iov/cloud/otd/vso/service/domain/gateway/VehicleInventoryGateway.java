package net.hwyz.iov.cloud.otd.vso.service.domain.gateway;

/**
 * 车辆库存网关接口
 */
public interface VehicleInventoryGateway {

    /**
     * 校验VIN是否可用
     * @param vin 车辆识别码
     * @return true=可用，false=不可用
     */
    boolean validateVinAvailable(String vin);

    /**
     * 更新车辆状态为已分配
     * @param vin 车辆识别码
     * @return 是否成功
     */
    boolean updateVehicleStatusToAllocated(String vin);

    /**
     * 释放车辆状态
     * @param vin 车辆识别码
     * @return 是否成功
     */
    boolean releaseVehicleStatus(String vin);

    /**
     * 获取车辆ID
     * @param vin 车辆识别码
     * @return 车辆ID
     */
    String getVehicleIdByVin(String vin);
}