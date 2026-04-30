package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderAssignmentPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 订单归属 Mapper 接口
 *
 * @author VSO Team
 */
@Mapper
public interface OrderAssignmentMapper extends BaseMapper<OrderAssignmentPo> {

    /**
     * 根据订单业务 ID 查询归属信息
     *
     * @param orderId 订单业务 ID
     * @return 订单归属 PO
     */
    OrderAssignmentPo selectByOrderId(@Param("orderId") String orderId);

}
