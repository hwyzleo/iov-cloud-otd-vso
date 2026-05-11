package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import net.hwyz.iov.cloud.framework.mysql.dao.BaseDao;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.ApprovalPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 审批单 Mapper 接口
 */
@Mapper
public interface ApprovalMapper extends BaseDao<ApprovalPo, Long> {

    ApprovalPo selectByApprovalNo(@Param("approvalNo") String approvalNo);

    ApprovalPo selectPoById(Long id);

    List<ApprovalPo> selectPoByMap(@Param("params") Map<String, Object> params);

    int countPoByMap(@Param("params") Map<String, Object> params);

    int insertPo(ApprovalPo entity);

    int batchInsertPo(List<ApprovalPo> entities);

    int updatePo(ApprovalPo entity);

    int logicalDeletePo(Long id);

    int physicalDeletePo(Long id);

    int batchPhysicalDeletePo(@Param("array") Long[] ids);

    List<ApprovalPo> selectPoByExample(ApprovalPo example);

    List<ApprovalPo> selectByOrderId(@Param("orderId") String orderId);

}
