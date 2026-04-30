package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderVehicleSnapshotRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.OrderVehicleSnapshotMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderVehicleSnapshotPo;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 订单车型配置快照仓储实现
 */
@Repository
@RequiredArgsConstructor
public class OrderVehicleSnapshotRepositoryImpl implements OrderVehicleSnapshotRepository {

    private final OrderVehicleSnapshotMapper orderVehicleSnapshotMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderVehicleSnapshotPo save(OrderVehicleSnapshotPo orderVehicleSnapshotPo) {
        if (orderVehicleSnapshotPo.getId() == null) {
            orderVehicleSnapshotMapper.insert(orderVehicleSnapshotPo);
        } else {
            orderVehicleSnapshotMapper.updateById(orderVehicleSnapshotPo);
        }
        return orderVehicleSnapshotPo;
    }

    @Override
    public Optional<OrderVehicleSnapshotPo> findByOrderId(String orderId) {
        LambdaQueryWrapper<OrderVehicleSnapshotPo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderVehicleSnapshotPo::getOrderId, orderId)
               .eq(OrderVehicleSnapshotPo::getRowValid, 1);
        return Optional.ofNullable(orderVehicleSnapshotMapper.selectOne(wrapper));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(String snapshotId) {
        OrderVehicleSnapshotPo orderVehicleSnapshotPo = orderVehicleSnapshotMapper.selectById(snapshotId);
        if (orderVehicleSnapshotPo != null) {
            orderVehicleSnapshotPo.setRowValid(0);
            orderVehicleSnapshotMapper.updateById(orderVehicleSnapshotPo);
        }
    }

}
