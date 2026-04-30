package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderAmountPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 订单金额 Mapper 接口
 *
 * @author VSO Team
 */
@Mapper
public interface OrderAmountMapper extends BaseMapper<OrderAmountPo> {

    /**
     * 根据订单业务 ID 查询金额信息
     *
     * @param orderId 订单业务 ID
     * @return 订单金额 PO
     */
    OrderAmountPo selectByOrderId(@Param("orderId") String orderId);

}
