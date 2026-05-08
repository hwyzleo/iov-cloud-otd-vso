package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderMaterialRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.OrderMaterialMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderMaterialPo;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class OrderMaterialRepositoryImpl implements OrderMaterialRepository {

    private final OrderMaterialMapper orderMaterialMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderMaterialPo save(OrderMaterialPo orderMaterialPo) {
        if (orderMaterialPo.getId() == null) {
            orderMaterialMapper.insertPo(orderMaterialPo);
        } else {
            orderMaterialMapper.updatePo(orderMaterialPo);
        }
        return orderMaterialPo;
    }

    @Override
    public Optional<OrderMaterialPo> findByOrderIdAndType(String orderId, String materialType) {
        Map<String, Object> params = new HashMap<>();
        params.put("orderId", orderId);
        params.put("materialType", materialType);
        params.put("rowValid", 1);
        return orderMaterialMapper.selectPoByMap(params).stream().findFirst();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(String materialId) {
        OrderMaterialPo orderMaterialPo = orderMaterialMapper.selectPoById(Long.valueOf(materialId));
        if (orderMaterialPo != null) {
            orderMaterialPo.setRowValid(0);
            orderMaterialMapper.updatePo(orderMaterialPo);
        }
    }

}