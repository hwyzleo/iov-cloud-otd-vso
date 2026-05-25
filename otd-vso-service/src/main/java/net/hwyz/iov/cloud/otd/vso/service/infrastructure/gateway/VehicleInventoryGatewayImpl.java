package net.hwyz.iov.cloud.otd.vso.service.infrastructure.gateway;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.otd.vso.service.domain.gateway.VehicleInventoryGateway;
import org.springframework.stereotype.Component;

/**
 * 车辆库存网关实现
 * TODO: 后续对接真实车辆库存服务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VehicleInventoryGatewayImpl implements VehicleInventoryGateway {

    @Override
    public boolean validateVinAvailable(String vin) {
        log.info("校验VIN可用性: vin={}", vin);
        return true;
    }

    @Override
    public boolean updateVehicleStatusToAllocated(String vin) {
        log.info("更新车辆状态为已分配: vin={}", vin);
        return true;
    }

    @Override
    public boolean releaseVehicleStatus(String vin) {
        log.info("释放车辆状态: vin={}", vin);
        return true;
    }

    @Override
    public String getVehicleIdByVin(String vin) {
        log.info("获取车辆ID: vin={}", vin);
        return vin;
    }
}