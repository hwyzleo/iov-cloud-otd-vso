package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import net.hwyz.iov.cloud.framework.mysql.dao.BaseDao;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.CallbackLogPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 外部回调日志 Mapper 接口
 */
@Mapper
public interface CallbackLogMapper extends BaseDao<CallbackLogPo, Long> {

    CallbackLogPo selectByIdempotentKey(@Param("idempotentKey") String idempotentKey);

    CallbackLogPo selectPoById(Long id);

    List<CallbackLogPo> selectPoByMap(@Param("params") Map<String, Object> params);

    int countPoByMap(@Param("params") Map<String, Object> params);

    int insertPo(CallbackLogPo entity);

    int batchInsertPo(List<CallbackLogPo> entities);

    int updatePo(CallbackLogPo entity);

    int logicalDeletePo(Long id);

    int physicalDeletePo(Long id);

    int batchPhysicalDeletePo(@Param("array") Long[] ids);

    List<CallbackLogPo> selectPoByExample(CallbackLogPo example);

}
