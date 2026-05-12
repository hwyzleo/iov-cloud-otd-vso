package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import net.hwyz.iov.cloud.framework.mysql.dao.BaseDao;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.ApprovalRecordPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 审批流转记录 Mapper 接口
 */
@Mapper
public interface ApprovalRecordMapper extends BaseDao<ApprovalRecordPo, Long> {

    List<ApprovalRecordPo> selectByApprovalId(@Param("approvalId") String approvalId);

    ApprovalRecordPo selectPoById(Long id);

    List<ApprovalRecordPo> selectPoByMap(@Param("params") Map<String, Object> params);

    int countPoByMap(@Param("params") Map<String, Object> params);

    int insertPo(ApprovalRecordPo entity);

    int batchInsertPo(List<ApprovalRecordPo> entities);

    int updatePo(ApprovalRecordPo entity);

    int logicalDeletePo(Long id);

    int physicalDeletePo(Long id);

    List<ApprovalRecordPo> selectPoByExample(ApprovalRecordPo example);

}
