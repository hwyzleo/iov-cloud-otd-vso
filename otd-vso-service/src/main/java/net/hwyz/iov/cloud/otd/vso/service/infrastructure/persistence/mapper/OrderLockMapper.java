package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderLockPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 订单锁 Mapper 接口
 */
@Mapper
public interface OrderLockMapper extends BaseMapper<OrderLockPo> {

    OrderLockPo selectByOrderIdAndScene(@Param("orderId") String orderId, @Param("lockScene") String lockScene);

}
