package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.ExceptionOrderRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.ExceptionOrderMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.ExceptionOrderPo;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ExceptionOrderRepositoryImpl implements ExceptionOrderRepository {

    private final ExceptionOrderMapper exceptionOrderMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExceptionOrderPo save(ExceptionOrderPo exceptionOrderPo) {
        if (exceptionOrderPo.getId() == null) {
            exceptionOrderMapper.insertPo(exceptionOrderPo);
        } else {
            exceptionOrderMapper.updatePo(exceptionOrderPo);
        }
        return exceptionOrderPo;
    }

    @Override
    public Optional<ExceptionOrderPo> findByExceptionNo(String exceptionNo) {
        Map<String, Object> params = new HashMap<>();
        params.put("exceptionNo", exceptionNo);
        params.put("rowValid", 1);
        return exceptionOrderMapper.selectPoByMap(params).stream().findFirst();
    }

    @Override
    public Optional<ExceptionOrderPo> findByOrderId(String orderId) {
        Map<String, Object> params = new HashMap<>();
        params.put("orderId", orderId);
        params.put("rowValid", 1);
        params.put("orderBy", "discoverTime DESC");
        return exceptionOrderMapper.selectPoByMap(params).stream().findFirst();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(String exceptionOrderId) {
        ExceptionOrderPo exceptionOrderPo = exceptionOrderMapper.selectPoById(Long.valueOf(exceptionOrderId));
        if (exceptionOrderPo != null) {
            exceptionOrderPo.setRowValid(0);
            exceptionOrderMapper.updatePo(exceptionOrderPo);
        }
    }

}