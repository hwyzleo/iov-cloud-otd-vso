package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderAmountRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.OrderAmountMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderAmountPo;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class OrderAmountRepositoryImpl implements OrderAmountRepository {

    private final OrderAmountMapper orderAmountMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderAmountPo save(OrderAmountPo orderAmountPo) {
        if (orderAmountPo.getId() == null) {
            orderAmountMapper.insertPo(orderAmountPo);
        } else {
            orderAmountMapper.updatePo(orderAmountPo);
        }
        return orderAmountPo;
    }

    @Override
    public Optional<OrderAmountPo> findByOrderId(String orderId) {
        Map<String, Object> params = new HashMap<>();
        params.put("orderId", orderId);
        params.put("rowValid", 1);
        return orderAmountMapper.selectPoByMap(params).stream().findFirst();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(String orderId) {
        Map<String, Object> params = new HashMap<>();
        params.put("orderId", orderId);
        params.put("rowValid", 1);
        OrderAmountPo orderAmountPo = orderAmountMapper.selectPoByMap(params).stream().findFirst().orElse(null);
        if (orderAmountPo != null) {
            orderAmountPo.setRowValid(0);
            orderAmountMapper.updatePo(orderAmountPo);
        }
    }

}