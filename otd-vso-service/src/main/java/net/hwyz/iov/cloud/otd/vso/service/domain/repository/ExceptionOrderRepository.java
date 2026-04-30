package net.hwyz.iov.cloud.otd.vso.service.domain.repository;

import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.ExceptionOrderPo;

import java.util.Optional;

/**
 * 异常单仓储接口
 */
public interface ExceptionOrderRepository {

    ExceptionOrderPo save(ExceptionOrderPo exceptionOrderPo);

    Optional<ExceptionOrderPo> findByExceptionNo(String exceptionNo);

    Optional<ExceptionOrderPo> findByOrderId(String orderId);

    void delete(String exceptionOrderId);

}
