package net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.framework.common.domain.AbstractRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.order.model.OrderModelConfigDo;
import net.hwyz.iov.cloud.otd.vso.service.domain.order.repository.OrderModelConfigRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.assembler.OrderModelConfigPoAssembler;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.dao.OrderModelConfigDao;
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
public class OrderModelConfigRepositoryImpl extends AbstractRepository<Long, OrderModelConfigDo> implements OrderModelConfigRepository {

    private final OrderModelConfigDao orderModelConfigDao;

    @Override
    public Optional<OrderModelConfigDo> getById(Long aLong) {
        return Optional.empty();
    }

    @Override
    public boolean save(OrderModelConfigDo orderModelConfigDo) {
        switch (orderModelConfigDo.getState()) {
            case NEW -> orderModelConfigDao.insertPo(OrderModelConfigPoAssembler.INSTANCE.fromDo(orderModelConfigDo));
            default -> {
                return false;
            }
        }
        return true;
    }
}
