package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.ConfigTimeoutPo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 超时任务配置表 Mapper 接口
 */
@Mapper
public interface ConfigTimeoutMapper extends BaseMapper<ConfigTimeoutPo> {

}
