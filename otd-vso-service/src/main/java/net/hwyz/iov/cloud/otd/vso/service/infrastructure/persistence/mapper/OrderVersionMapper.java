package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderVersionPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 订单版本 Mapper 接口
 */
@Mapper
public interface OrderVersionMapper extends BaseMapper<OrderVersionPo> {

    List<OrderVersionPo> selectByOrderId(@Param("orderId") String orderId);

}
