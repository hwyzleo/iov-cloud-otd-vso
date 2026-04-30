package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderAmountRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.OrderAmountMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderAmountPo;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 订单金额仓储实现
 *
 * @author VSO Team
 */
@Repository
@RequiredArgsConstructor
public class OrderAmountRepositoryImpl implements OrderAmountRepository {

    private final OrderAmountMapper orderAmountMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderAmountPo save(OrderAmountPo orderAmountPo) {
        if (orderAmountPo.getId() == null) {
            orderAmountMapper.insert(orderAmountPo);
        } else {
            orderAmountMapper.updateById(orderAmountPo);
        }
        return orderAmountPo;
    }

    @Override
    public Optional<OrderAmountPo> findByOrderId(String orderId) {
        LambdaQueryWrapper<OrderAmountPo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderAmountPo::getOrderId, orderId)
               .eq(OrderAmountPo::getRowValid, 1);
        return Optional.ofNullable(orderAmountMapper.selectOne(wrapper));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(String orderId) {
        OrderAmountPo orderAmountPo = orderAmountMapper.selectById(orderId);
        if (orderAmountPo != null) {
            orderAmountPo.setRowValid(0);
            orderAmountMapper.updateById(orderAmountPo);
        }
    }

}
