package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.RegistrationPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 上牌跟踪 Mapper 接口
 */
@Mapper
public interface RegistrationMapper extends BaseMapper<RegistrationPo> {

    RegistrationPo selectByOrderId(@Param("orderId") String orderId);

}
