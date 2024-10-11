package net.hwyz.iov.cloud.otd.vso.service.infrastructure.external;

import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.otd.vso.service.domain.external.service.ExVehicleModelConfigService;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Service;

/**
 * 外部车系车型配置服务回退处理
 *
 * @author hwyz_leo
 */
@Slf4j
@Service
public class ExVehicleModelConfigServiceFallbackFactory implements FallbackFactory<ExVehicleModelConfigService> {

    @Override
    public ExVehicleModelConfigService create(Throwable cause) {
        return new ExVehicleModelConfigService() {

            @Override
            public String getVehicleModeConfigCode(String modelCode, String exteriorCode, String interiorCode, String wheelCode,
                                                   String spareTireCode, String adasCode) {
                if (logger.isDebugEnabled()) {
                    logger.warn("根据车型配置类型得到匹配的车型配置代码异常", cause);
                } else {
                    logger.warn("根据车型配置类型得到匹配的车型配置代码异常:[{}]", cause.getMessage());
                }
                return null;
            }

        };
    }

}
