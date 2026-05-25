package net.hwyz.iov.cloud.otd.vso.service.domain.repository;

import java.util.Optional;

/**
 * VIN占用有效期配置仓储接口
 */
public interface VehicleOccupancyConfigRepository {

    /**
     * 获取默认占用小时数
     * @return 占用小时数，默认72小时
     */
    int getDefaultOccupancyHours();

    /**
     * 根据规则键获取配置
     * @param ruleKey 规则键
     * @return 配置信息
     */
    Optional<Integer> getOccupancyHoursByRuleKey(String ruleKey);
}