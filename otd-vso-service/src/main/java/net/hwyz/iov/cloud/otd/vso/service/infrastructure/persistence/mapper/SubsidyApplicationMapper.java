package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import net.hwyz.iov.cloud.framework.mysql.dao.BaseDao;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SubsidyApplicationPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 补贴申请 Mapper 接口
 */
@Mapper
public interface SubsidyApplicationMapper extends BaseDao<SubsidyApplicationPo, Long> {

    SubsidyApplicationPo selectByOrderId(@Param("orderId") String orderId);

    SubsidyApplicationPo selectPoById(Long id);

    List<SubsidyApplicationPo> selectPoByMap(@Param("params") Map<String, Object> params);

    int countPoByMap(@Param("params") Map<String, Object> params);

    int insertPo(SubsidyApplicationPo entity);

    int batchInsertPo(List<SubsidyApplicationPo> entities);

    int updatePo(SubsidyApplicationPo entity);

    int logicalDeletePo(Long id);

    int physicalDeletePo(Long id);

    List<SubsidyApplicationPo> selectPoByExample(SubsidyApplicationPo example);

}
