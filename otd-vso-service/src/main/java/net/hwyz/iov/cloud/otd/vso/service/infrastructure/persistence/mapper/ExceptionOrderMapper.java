package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.ExceptionOrderPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 异常单 Mapper 接口
 */
@Mapper
public interface ExceptionOrderMapper extends BaseMapper<ExceptionOrderPo> {

    ExceptionOrderPo selectByExceptionNo(@Param("exceptionNo") String exceptionNo);

}
