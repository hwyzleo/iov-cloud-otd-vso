package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderStatusDimensionPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 订单维度状态 Mapper 接口
 *
 * @author VSO Team
 */
@Mapper
public interface OrderStatusDimensionMapper extends BaseMapper<OrderStatusDimensionPo> {

    /**
     * 根据订单业务 ID 查询维度状态
     *
     * @param orderId 订单业务 ID
     * @return 订单维度状态 PO
     */
    OrderStatusDimensionPo selectByOrderId(@Param("orderId") String orderId);

}
