package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.AuditLogPo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统审计日志表 Mapper 接口
 */
@Mapper
public interface AuditLogMapper extends BaseMapper<AuditLogPo> {

}
