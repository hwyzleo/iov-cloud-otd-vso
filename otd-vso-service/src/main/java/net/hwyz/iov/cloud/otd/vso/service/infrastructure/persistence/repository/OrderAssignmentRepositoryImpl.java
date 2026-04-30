package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderAssignmentRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.OrderAssignmentMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderAssignmentPo;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 订单归属仓储实现
 */
@Repository
@RequiredArgsConstructor
public class OrderAssignmentRepositoryImpl implements OrderAssignmentRepository {

    private final OrderAssignmentMapper orderAssignmentMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderAssignmentPo save(OrderAssignmentPo orderAssignmentPo) {
        if (orderAssignmentPo.getId() == null) {
            orderAssignmentMapper.insert(orderAssignmentPo);
        } else {
            orderAssignmentMapper.updateById(orderAssignmentPo);
        }
        return orderAssignmentPo;
    }

    @Override
    public Optional<OrderAssignmentPo> findByOrderId(String orderId) {
        LambdaQueryWrapper<OrderAssignmentPo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderAssignmentPo::getOrderId, orderId)
               .eq(OrderAssignmentPo::getRowValid, 1);
        return Optional.ofNullable(orderAssignmentMapper.selectOne(wrapper));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(String assignmentId) {
        OrderAssignmentPo orderAssignmentPo = orderAssignmentMapper.selectById(assignmentId);
        if (orderAssignmentPo != null) {
            orderAssignmentPo.setRowValid(0);
            orderAssignmentMapper.updateById(orderAssignmentPo);
        }
    }

}
