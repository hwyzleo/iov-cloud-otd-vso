package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.NotifyTaskPo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 通知任务表 Mapper 接口
 */
@Mapper
public interface NotifyTaskMapper extends BaseMapper<NotifyTaskPo> {

}
