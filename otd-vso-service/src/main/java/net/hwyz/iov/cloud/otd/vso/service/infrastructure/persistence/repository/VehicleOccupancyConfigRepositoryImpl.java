package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.VehicleOccupancyConfigRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.VehicleOccupancyConfigMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.VehicleOccupancyConfigPo;
import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import java.util.Optional;

/**
 * VIN占用有效期配置仓储实现
 */
@Repository
@RequiredArgsConstructor
public class VehicleOccupancyConfigRepositoryImpl implements VehicleOccupancyConfigRepository {

    private final VehicleOccupancyConfigMapper mapper;
    private static final int DEFAULT_HOURS = 72;

    @Override
    public int getDefaultOccupancyHours() {
        VehicleOccupancyConfigPo config = mapper.selectOne(
            new LambdaQueryWrapper<VehicleOccupancyConfigPo>()
                .eq(VehicleOccupancyConfigPo::getOccupancyRuleKey, "GENERAL_RULE")
                .eq(VehicleOccupancyConfigPo::getEnableFlag, 1)
        );
        if (config != null && config.getOccupancyHours() != null) {
            return config.getOccupancyHours();
        }
        return DEFAULT_HOURS;
    }

    @Override
    public Optional<Integer> getOccupancyHoursByRuleKey(String ruleKey) {
        VehicleOccupancyConfigPo config = mapper.selectOne(
            new LambdaQueryWrapper<VehicleOccupancyConfigPo>()
                .eq(VehicleOccupancyConfigPo::getOccupancyRuleKey, ruleKey)
                .eq(VehicleOccupancyConfigPo::getEnableFlag, 1)
        );
        if (config != null && config.getOccupancyHours() != null) {
            return Optional.of(config.getOccupancyHours());
        }
        return Optional.empty();
    }
}