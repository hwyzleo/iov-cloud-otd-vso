package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.ExceptionOrderRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.ExceptionOrderMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.ExceptionOrderPo;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 异常单仓储实现
 */
@Repository
@RequiredArgsConstructor
public class ExceptionOrderRepositoryImpl implements ExceptionOrderRepository {

    private final ExceptionOrderMapper exceptionOrderMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExceptionOrderPo save(ExceptionOrderPo exceptionOrderPo) {
        if (exceptionOrderPo.getId() == null) {
            exceptionOrderMapper.insert(exceptionOrderPo);
        } else {
            exceptionOrderMapper.updateById(exceptionOrderPo);
        }
        return exceptionOrderPo;
    }

    @Override
    public Optional<ExceptionOrderPo> findByExceptionNo(String exceptionNo) {
        LambdaQueryWrapper<ExceptionOrderPo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExceptionOrderPo::getExceptionNo, exceptionNo)
               .eq(ExceptionOrderPo::getRowValid, 1);
        return Optional.ofNullable(exceptionOrderMapper.selectOne(wrapper));
    }

    @Override
    public Optional<ExceptionOrderPo> findByOrderId(String orderId) {
        LambdaQueryWrapper<ExceptionOrderPo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExceptionOrderPo::getOrderId, orderId)
               .eq(ExceptionOrderPo::getRowValid, 1)
               .orderByDesc(ExceptionOrderPo::getDiscoverTime);
        return Optional.ofNullable(exceptionOrderMapper.selectOne(wrapper));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(String exceptionOrderId) {
        ExceptionOrderPo exceptionOrderPo = exceptionOrderMapper.selectById(exceptionOrderId);
        if (exceptionOrderPo != null) {
            exceptionOrderPo.setRowValid(0);
            exceptionOrderMapper.updateById(exceptionOrderPo);
        }
    }

}
