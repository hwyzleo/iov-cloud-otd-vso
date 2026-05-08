package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderStatusDimensionRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.OrderStatusDimensionMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderStatusDimensionPo;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class OrderStatusDimensionRepositoryImpl implements OrderStatusDimensionRepository {

    private final OrderStatusDimensionMapper orderStatusDimensionMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderStatusDimensionPo save(OrderStatusDimensionPo orderStatusDimensionPo) {
        if (orderStatusDimensionPo.getId() == null) {
            orderStatusDimensionMapper.insertPo(orderStatusDimensionPo);
        } else {
            orderStatusDimensionMapper.updatePo(orderStatusDimensionPo);
        }
        return orderStatusDimensionPo;
    }

    @Override
    public Optional<OrderStatusDimensionPo> findByOrderId(String orderId) {
        Map<String, Object> params = new HashMap<>();
        params.put("orderId", orderId);
        params.put("rowValid", 1);
        return orderStatusDimensionMapper.selectPoByMap(params).stream().findFirst();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(String statusDimensionId) {
        OrderStatusDimensionPo orderStatusDimensionPo = orderStatusDimensionMapper.selectPoById(Long.valueOf(statusDimensionId));
        if (orderStatusDimensionPo != null) {
            orderStatusDimensionPo.setRowValid(0);
            orderStatusDimensionMapper.updatePo(orderStatusDimensionPo);
        }
    }

}