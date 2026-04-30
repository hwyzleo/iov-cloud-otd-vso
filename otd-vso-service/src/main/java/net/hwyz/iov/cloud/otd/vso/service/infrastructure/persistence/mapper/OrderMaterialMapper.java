package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderMaterialPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 订单资料 Mapper 接口
 */
@Mapper
public interface OrderMaterialMapper extends BaseMapper<OrderMaterialPo> {

    OrderMaterialPo selectByOrderIdAndType(@Param("orderId") String orderId, @Param("materialType") String materialType);

}
