package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Order;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderState;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.converter.OrderPoConverter;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.OrderMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderPo;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 订单仓储实现
 *
 * @author VSO Team
 */
@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {

    private final OrderMapper orderMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Order save(Order order) {
        OrderPo orderPo = OrderPoConverter.INSTANCE.fromDomain(order);
        OrderPo existingPo = orderMapper.selectByOrderId(order.getId());
        if (existingPo == null) {
            orderMapper.insertPo(orderPo);
        } else {
            orderPo.setId(existingPo.getId());
            orderMapper.updatePo(orderPo);
        }
        return OrderPoConverter.INSTANCE.toDomain(orderPo);
    }

    @Override
    public Optional<Order> findByOrderId(String orderId) {
        return Optional.ofNullable(orderMapper.selectByOrderId(orderId))
                .map(OrderPoConverter.INSTANCE::toDomain);
    }

    @Override
    public Optional<Order> findByOrderNo(String orderNo) {
        return Optional.ofNullable(orderMapper.selectByOrderNo(orderNo))
                .map(OrderPoConverter.INSTANCE::toDomain);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByOrderNo(String orderNo) {
        orderMapper.logicalDeletePoByOrderNo(orderNo);
    }

    @Override
    public List<Order> search(String orderNo, OrderState orderState, List<OrderState> orderStateRange,
                                Boolean hasDeliveryPerson, Date beginTime, Date endTime) {
        java.util.Map<String, Object> params = new java.util.HashMap<>();
        
        if (orderNo != null && !orderNo.isEmpty()) {
            params.put("orderNo", orderNo);
        }
        
        if (orderState != null) {
            params.put("orderState", orderState.getValue());
        }
        
        if (orderStateRange != null && !orderStateRange.isEmpty()) {
            params.put("orderStateRange", orderStateRange.stream()
                    .map(OrderState::getValue)
                    .toList());
        }
        
        if (hasDeliveryPerson != null) {
            params.put("hasDeliveryPerson", hasDeliveryPerson);
        }
        
        if (beginTime != null) {
            params.put("beginTime", beginTime);
        }
        
        if (endTime != null) {
            params.put("endTime", endTime);
        }
        
        return orderMapper.selectPoByMap(params).stream()
                .map(OrderPoConverter.INSTANCE::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Integer count(String deliveryPersonId, Boolean delivered) {
        java.util.Map<String, Object> params = new java.util.HashMap<>();
        if (deliveryPersonId != null && !deliveryPersonId.isEmpty()) {
            params.put("deliveryPersonId", deliveryPersonId);
        }
        if (delivered != null) {
            params.put("delivered", delivered);
        }
        
        return orderMapper.countPoByMap(params);
    }

    @Override
    public Optional<Order> findByOrderNoAndAccountId(String orderNo, String accountId) {
        return Optional.ofNullable(orderMapper.selectByOrderNoAndAccountId(orderNo, accountId))
                .map(OrderPoConverter.INSTANCE::toDomain);
    }

    @Override
    public List<Order> findByAccountId(String accountId, String type) {
        // TODO: 需要通过 vso_order_party 表关联查询
        throw new UnsupportedOperationException("待通过 vso_order_party 表关联查询实现");
    }

}
