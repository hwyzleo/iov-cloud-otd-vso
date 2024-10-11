package net.hwyz.iov.cloud.otd.vso.service.domain.external.service;

import net.hwyz.iov.cloud.otd.vso.service.infrastructure.external.ExVehicleModelConfigServiceFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 外部车系车型配置服务
 *
 * @author hwyz_leo
 */
@FeignClient(name = "vmd-service", path = "/service/vehicleModelConfig", fallbackFactory = ExVehicleModelConfigServiceFallbackFactory.class)
public interface ExVehicleModelConfigService {

    /**
     * 根据车型配置类型得到匹配的车型配置代码
     *
     * @param modelCode     车型代码
     * @param exteriorCode  外饰代码
     * @param interiorCode  内饰代码
     * @param wheelCode     车轮代码
     * @param spareTireCode 备胎代码
     * @param adasCode      智驾代码
     * @return 车型配置代码
     */
    @GetMapping("/modelConfigCode")
    String getVehicleModeConfigCode(@RequestParam String modelCode, @RequestParam String exteriorCode,
                                    @RequestParam String interiorCode, @RequestParam String wheelCode,
                                    @RequestParam String spareTireCode, @RequestParam String adasCode);

}
