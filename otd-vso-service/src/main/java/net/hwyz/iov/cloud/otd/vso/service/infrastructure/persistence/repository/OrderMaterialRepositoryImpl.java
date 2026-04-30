package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderMaterialRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.OrderMaterialMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderMaterialPo;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 订单资料仓储实现
 */
@Repository
@RequiredArgsConstructor
public class OrderMaterialRepositoryImpl implements OrderMaterialRepository {

    private final OrderMaterialMapper orderMaterialMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderMaterialPo save(OrderMaterialPo orderMaterialPo) {
        if (orderMaterialPo.getId() == null) {
            orderMaterialMapper.insert(orderMaterialPo);
        } else {
            orderMaterialMapper.updateById(orderMaterialPo);
        }
        return orderMaterialPo;
    }

    @Override
    public Optional<OrderMaterialPo> findByOrderIdAndType(String orderId, String materialType) {
        LambdaQueryWrapper<OrderMaterialPo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderMaterialPo::getOrderId, orderId)
               .eq(OrderMaterialPo::getMaterialType, materialType)
               .eq(OrderMaterialPo::getRowValid, 1);
        return Optional.ofNullable(orderMaterialMapper.selectOne(wrapper));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(String materialId) {
        OrderMaterialPo orderMaterialPo = orderMaterialMapper.selectById(materialId);
        if (orderMaterialPo != null) {
            orderMaterialPo.setRowValid(0);
            orderMaterialMapper.updateById(orderMaterialPo);
        }
    }

}
