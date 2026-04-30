package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderPartyRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.OrderPartyMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderPartyPo;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 订单客户仓储实现
 */
@Repository
@RequiredArgsConstructor
public class OrderPartyRepositoryImpl implements OrderPartyRepository {

    private final OrderPartyMapper orderPartyMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderPartyPo save(OrderPartyPo orderPartyPo) {
        if (orderPartyPo.getId() == null) {
            orderPartyMapper.insert(orderPartyPo);
        } else {
            orderPartyMapper.updateById(orderPartyPo);
        }
        return orderPartyPo;
    }

    @Override
    public Optional<OrderPartyPo> findByOrderIdAndRole(String orderId, String partyRole) {
        LambdaQueryWrapper<OrderPartyPo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderPartyPo::getOrderId, orderId)
               .eq(OrderPartyPo::getPartyRole, partyRole)
               .eq(OrderPartyPo::getRowValid, 1);
        return Optional.ofNullable(orderPartyMapper.selectOne(wrapper));
    }

    @Override
    public List<OrderPartyPo> findByOrderId(String orderId) {
        LambdaQueryWrapper<OrderPartyPo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderPartyPo::getOrderId, orderId)
               .eq(OrderPartyPo::getRowValid, 1);
        return orderPartyMapper.selectList(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(String partyId) {
        OrderPartyPo orderPartyPo = orderPartyMapper.selectById(partyId);
        if (orderPartyPo != null) {
            orderPartyPo.setRowValid(0);
            orderPartyMapper.updateById(orderPartyPo);
        }
    }

}
