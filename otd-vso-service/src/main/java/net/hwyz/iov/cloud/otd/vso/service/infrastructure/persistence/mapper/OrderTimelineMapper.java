package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderTimelinePo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 订单时间线 Mapper 接口
 */
@Mapper
public interface OrderTimelineMapper extends BaseMapper<OrderTimelinePo> {

    List<OrderTimelinePo> selectByOrderId(@Param("orderId") String orderId);

}
