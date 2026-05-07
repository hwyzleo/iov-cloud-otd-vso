package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderTransformPo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 小订单转正式订单转化关系表 Mapper 接口
 */
@Mapper
public interface OrderTransformMapper extends BaseMapper<OrderTransformPo> {

}
