package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderShadowDeletePo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 物理删除审计影子记录表 Mapper 接口
 */
@Mapper
public interface OrderShadowDeleteMapper extends BaseMapper<OrderShadowDeletePo> {

}
