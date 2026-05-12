package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import net.hwyz.iov.cloud.framework.mysql.dao.BaseDao;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.AuditLogPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 系统审计日志表 Mapper 接口
 */
@Mapper
public interface AuditLogMapper extends BaseDao<AuditLogPo, Long> {

    AuditLogPo selectPoById(Long id);

    List<AuditLogPo> selectPoByMap(@Param("params") Map<String, Object> params);

    int countPoByMap(@Param("params") Map<String, Object> params);

    int insertPo(AuditLogPo entity);

    int batchInsertPo(List<AuditLogPo> entities);

    int updatePo(AuditLogPo entity);

    int logicalDeletePo(Long id);

    int physicalDeletePo(Long id);

    List<AuditLogPo> selectPoByExample(AuditLogPo example);

    List<AuditLogPo> selectByOrderId(@Param("orderId") String orderId);

}
