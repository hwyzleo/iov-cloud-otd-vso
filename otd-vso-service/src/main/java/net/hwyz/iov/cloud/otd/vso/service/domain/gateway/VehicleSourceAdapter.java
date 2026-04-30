package net.hwyz.iov.cloud.otd.vso.service.domain.gateway;

/**
 * 车源适配器
 *
 * @author VSO Team
 */
public interface VehicleSourceAdapter {

    /**
     * 查询可用车源
     *
     * @param modelCode 车型编码
     * @param configCode 配置编码
     * @param colorCode 颜色编码
     * @return 可用车源列表（JSON）
     */
    String queryAvailableVehicles(String modelCode, String configCode, String colorCode);

    /**
     * 占用车源
     *
     * @param vehicleId 车辆 ID
     * @param orderId 订单 ID
     * @param expireHours 占用时长（小时）
     * @return 是否成功
     */
    boolean occupyVehicle(String vehicleId, String orderId, Integer expireHours);

    /**
     * 释放车源
     *
     * @param vehicleId 车辆 ID
     * @param orderId 订单 ID
     */
    void releaseVehicle(String vehicleId, String orderId);

    /**
     * 查询车辆信息
     *
     * @param vin VIN
     * @return 车辆信息（JSON）
     */
    String queryVehicleInfo(String vin);

}
