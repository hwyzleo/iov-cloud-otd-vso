package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderAssignmentRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.OrderAssignmentMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderAssignmentPo;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class OrderAssignmentRepositoryImpl implements OrderAssignmentRepository {

    private final OrderAssignmentMapper orderAssignmentMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderAssignmentPo save(OrderAssignmentPo orderAssignmentPo) {
        if (orderAssignmentPo.getId() == null) {
            orderAssignmentMapper.insertPo(orderAssignmentPo);
        } else {
            orderAssignmentMapper.updatePo(orderAssignmentPo);
        }
        return orderAssignmentPo;
    }

    @Override
    public Optional<OrderAssignmentPo> findByOrderId(String orderId) {
        Map<String, Object> params = new HashMap<>();
        params.put("orderId", orderId);
        params.put("rowValid", 1);
        return orderAssignmentMapper.selectPoByMap(params).stream().findFirst();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(String assignmentId) {
        OrderAssignmentPo orderAssignmentPo = orderAssignmentMapper.selectPoById(Long.valueOf(assignmentId));
        if (orderAssignmentPo != null) {
            orderAssignmentPo.setRowValid(0);
            orderAssignmentMapper.updatePo(orderAssignmentPo);
        }
    }

}