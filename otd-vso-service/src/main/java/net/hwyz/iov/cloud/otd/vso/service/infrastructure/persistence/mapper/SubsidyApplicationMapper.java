package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SubsidyApplicationPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 补贴申请 Mapper 接口
 */
@Mapper
public interface SubsidyApplicationMapper extends BaseMapper<SubsidyApplicationPo> {

    SubsidyApplicationPo selectByOrderId(@Param("orderId") String orderId);

}
