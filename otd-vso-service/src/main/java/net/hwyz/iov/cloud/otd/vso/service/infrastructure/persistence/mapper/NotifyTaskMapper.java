package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import net.hwyz.iov.cloud.framework.mysql.dao.BaseDao;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.NotifyTaskPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 通知任务表 Mapper 接口
 */
@Mapper
public interface NotifyTaskMapper extends BaseDao<NotifyTaskPo, Long> {

    NotifyTaskPo selectPoById(Long id);

    List<NotifyTaskPo> selectPoByMap(@Param("params") Map<String, Object> params);

    int countPoByMap(@Param("params") Map<String, Object> params);

    int insertPo(NotifyTaskPo entity);

    int batchInsertPo(List<NotifyTaskPo> entities);

    int updatePo(NotifyTaskPo entity);

    int logicalDeletePo(Long id);

    int physicalDeletePo(Long id);

    int batchPhysicalDeletePo(@Param("array") Long[] ids);

    List<NotifyTaskPo> selectPoByExample(NotifyTaskPo example);

}
