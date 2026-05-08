package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderPartyRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.OrderPartyMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderPartyPo;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class OrderPartyRepositoryImpl implements OrderPartyRepository {

    private final OrderPartyMapper orderPartyMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderPartyPo save(OrderPartyPo orderPartyPo) {
        if (orderPartyPo.getId() == null) {
            orderPartyMapper.insertPo(orderPartyPo);
        } else {
            orderPartyMapper.updatePo(orderPartyPo);
        }
        return orderPartyPo;
    }

    @Override
    public Optional<OrderPartyPo> findByOrderIdAndRole(String orderId, String partyRole) {
        Map<String, Object> params = new HashMap<>();
        params.put("orderId", orderId);
        params.put("partyRole", partyRole);
        params.put("rowValid", 1);
        return orderPartyMapper.selectPoByMap(params).stream().findFirst();
    }

    @Override
    public List<OrderPartyPo> findByOrderId(String orderId) {
        Map<String, Object> params = new HashMap<>();
        params.put("orderId", orderId);
        params.put("rowValid", 1);
        return orderPartyMapper.selectPoByMap(params);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(String partyId) {
        OrderPartyPo orderPartyPo = orderPartyMapper.selectPoById(Long.valueOf(partyId));
        if (orderPartyPo != null) {
            orderPartyPo.setRowValid(0);
            orderPartyMapper.updatePo(orderPartyPo);
        }
    }

}