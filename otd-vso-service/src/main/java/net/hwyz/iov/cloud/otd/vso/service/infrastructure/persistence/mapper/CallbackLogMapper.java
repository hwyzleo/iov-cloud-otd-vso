package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.CallbackLogPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 外部回调日志 Mapper 接口
 */
@Mapper
public interface CallbackLogMapper extends BaseMapper<CallbackLogPo> {

    CallbackLogPo selectByIdempotentKey(@Param("idempotentKey") String idempotentKey);

}
