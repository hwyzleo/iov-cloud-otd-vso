package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderModelConfig;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderModelConfigRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.converter.OrderModelConfigPoConverter;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.OrderModelConfigMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 车辆销售订单车型配置领域仓库接口实现类
 *
 * @author hwyz_leo
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class OrderModelConfigRepositoryImpl implements OrderModelConfigRepository {

    private final OrderModelConfigMapper orderModelConfigMapper;

    @Override
    public Optional<OrderModelConfig> getById(Long aLong) {
        return Optional.empty();
    }

    @Override
    public boolean save(OrderModelConfig orderModelConfig) {
        var po = OrderModelConfigPoConverter.INSTANCE.fromDo(orderModelConfig);
        orderModelConfigMapper.insertPo(po);
        return true;
    }
}
