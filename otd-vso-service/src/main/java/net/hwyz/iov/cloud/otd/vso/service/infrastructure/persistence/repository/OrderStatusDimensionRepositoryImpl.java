package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderStatusDimensionRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.OrderStatusDimensionMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderStatusDimensionPo;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 订单维度状态仓储实现
 */
@Repository
@RequiredArgsConstructor
public class OrderStatusDimensionRepositoryImpl implements OrderStatusDimensionRepository {

    private final OrderStatusDimensionMapper orderStatusDimensionMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderStatusDimensionPo save(OrderStatusDimensionPo orderStatusDimensionPo) {
        if (orderStatusDimensionPo.getId() == null) {
            orderStatusDimensionMapper.insert(orderStatusDimensionPo);
        } else {
            orderStatusDimensionMapper.updateById(orderStatusDimensionPo);
        }
        return orderStatusDimensionPo;
    }

    @Override
    public Optional<OrderStatusDimensionPo> findByOrderId(String orderId) {
        LambdaQueryWrapper<OrderStatusDimensionPo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderStatusDimensionPo::getOrderId, orderId)
               .eq(OrderStatusDimensionPo::getRowValid, 1);
        return Optional.ofNullable(orderStatusDimensionMapper.selectOne(wrapper));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(String statusDimensionId) {
        OrderStatusDimensionPo orderStatusDimensionPo = orderStatusDimensionMapper.selectById(statusDimensionId);
        if (orderStatusDimensionPo != null) {
            orderStatusDimensionPo.setRowValid(0);
            orderStatusDimensionMapper.updateById(orderStatusDimensionPo);
        }
    }

}
