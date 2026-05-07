package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderVersionDiffPo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单版本差异表 Mapper 接口
 */
@Mapper
public interface OrderVersionDiffMapper extends BaseMapper<OrderVersionDiffPo> {

}
