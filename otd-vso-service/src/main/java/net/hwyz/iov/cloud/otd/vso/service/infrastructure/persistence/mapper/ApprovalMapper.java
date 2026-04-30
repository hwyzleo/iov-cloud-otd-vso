package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.ApprovalPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 审批单 Mapper 接口
 */
@Mapper
public interface ApprovalMapper extends BaseMapper<ApprovalPo> {

    ApprovalPo selectByApprovalNo(@Param("approvalNo") String approvalNo);

}
