package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderVehicleSnapshotRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.OrderVehicleSnapshotMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderVehicleSnapshotPo;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class OrderVehicleSnapshotRepositoryImpl implements OrderVehicleSnapshotRepository {

    private final OrderVehicleSnapshotMapper orderVehicleSnapshotMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderVehicleSnapshotPo save(OrderVehicleSnapshotPo orderVehicleSnapshotPo) {
        if (orderVehicleSnapshotPo.getId() == null) {
            orderVehicleSnapshotMapper.insertPo(orderVehicleSnapshotPo);
        } else {
            orderVehicleSnapshotMapper.updatePo(orderVehicleSnapshotPo);
        }
        return orderVehicleSnapshotPo;
    }

    @Override
    public Optional<OrderVehicleSnapshotPo> findByOrderId(String orderId) {
        Map<String, Object> params = new HashMap<>();
        params.put("orderId", orderId);
        params.put("rowValid", 1);
        return orderVehicleSnapshotMapper.selectPoByMap(params).stream().findFirst();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(String snapshotId) {
        OrderVehicleSnapshotPo orderVehicleSnapshotPo = orderVehicleSnapshotMapper.selectPoById(Long.valueOf(snapshotId));
        if (orderVehicleSnapshotPo != null) {
            orderVehicleSnapshotPo.setRowValid(0);
            orderVehicleSnapshotMapper.updatePo(orderVehicleSnapshotPo);
        }
    }

    @Override
    public Integer findMaxVersionByOrderId(String orderId) {
        Integer maxVersion = orderVehicleSnapshotMapper.selectMaxVersionByOrderId(orderId);
        return maxVersion != null ? maxVersion : 0;
    }

}