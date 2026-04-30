package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.ApprovalRecordPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 审批流转记录 Mapper 接口
 */
@Mapper
public interface ApprovalRecordMapper extends BaseMapper<ApprovalRecordPo> {

    List<ApprovalRecordPo> selectByApprovalId(@Param("approvalId") String approvalId);

}
