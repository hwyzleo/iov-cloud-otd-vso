package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.ContractPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 合同 Mapper 接口
 */
@Mapper
public interface ContractMapper extends BaseMapper<ContractPo> {

    ContractPo selectByOrderIdAndType(@Param("orderId") String orderId, @Param("contractType") String contractType);

}
