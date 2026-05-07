package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderModelConfig;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderModelConfigRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 车辆销售订单车型配置仓储实现
 * TODO: 待迁移到 vso_order_vehicle_snapshot 表
 */
@Repository
@RequiredArgsConstructor
public class OrderModelConfigRepositoryImpl implements OrderModelConfigRepository {

    @Override
    public Optional<OrderModelConfig> getById(Long id) {
        throw new UnsupportedOperationException("待迁移到 vso_order_vehicle_snapshot 表");
    }

    @Override
    public boolean save(OrderModelConfig entity) {
        throw new UnsupportedOperationException("待迁移到 vso_order_vehicle_snapshot 表");
    }
}
