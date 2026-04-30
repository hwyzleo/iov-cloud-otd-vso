package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.FinanceApplicationPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 金融申请 Mapper 接口
 */
@Mapper
public interface FinanceApplicationMapper extends BaseMapper<FinanceApplicationPo> {

    FinanceApplicationPo selectByOrderId(@Param("orderId") String orderId);

}
