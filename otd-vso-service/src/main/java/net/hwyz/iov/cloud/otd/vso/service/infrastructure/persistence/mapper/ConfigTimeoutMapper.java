package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import net.hwyz.iov.cloud.framework.mysql.dao.BaseDao;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.ConfigTimeoutPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 超时任务配置表 Mapper 接口
 */
@Mapper
public interface ConfigTimeoutMapper extends BaseDao<ConfigTimeoutPo, Long> {

    ConfigTimeoutPo selectPoById(Long id);

    List<ConfigTimeoutPo> selectPoByMap(@Param("params") Map<String, Object> params);

    int countPoByMap(@Param("params") Map<String, Object> params);

    int insertPo(ConfigTimeoutPo entity);

    int batchInsertPo(List<ConfigTimeoutPo> entities);

    int updatePo(ConfigTimeoutPo entity);

    int logicalDeletePo(Long id);

    int physicalDeletePo(Long id);

    List<ConfigTimeoutPo> selectPoByExample(ConfigTimeoutPo example);

}
