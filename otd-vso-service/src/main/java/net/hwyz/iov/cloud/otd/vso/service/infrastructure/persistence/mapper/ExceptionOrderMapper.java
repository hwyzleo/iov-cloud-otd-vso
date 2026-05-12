package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import net.hwyz.iov.cloud.framework.mysql.dao.BaseDao;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.ExceptionOrderPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 异常单 Mapper 接口
 */
@Mapper
public interface ExceptionOrderMapper extends BaseDao<ExceptionOrderPo, Long> {

    ExceptionOrderPo selectByExceptionNo(@Param("exceptionNo") String exceptionNo);

    ExceptionOrderPo selectPoById(Long id);

    List<ExceptionOrderPo> selectPoByMap(@Param("params") Map<String, Object> params);

    int countPoByMap(@Param("params") Map<String, Object> params);

    int insertPo(ExceptionOrderPo entity);

    int batchInsertPo(List<ExceptionOrderPo> entities);

    int updatePo(ExceptionOrderPo entity);

    int logicalDeletePo(Long id);

    int physicalDeletePo(Long id);

    List<ExceptionOrderPo> selectPoByExample(ExceptionOrderPo example);

    List<ExceptionOrderPo> selectByOrderId(@Param("orderId") String orderId);

}
