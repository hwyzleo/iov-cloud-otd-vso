package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderMaterialVersionPo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单资料版本表 Mapper 接口
 */
@Mapper
public interface OrderMaterialVersionMapper extends BaseMapper<OrderMaterialVersionPo> {

}
