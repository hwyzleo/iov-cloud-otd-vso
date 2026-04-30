package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderVehicleSnapshotPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 订单车型配置快照 Mapper 接口
 *
 * @author VSO Team
 */
@Mapper
public interface OrderVehicleSnapshotMapper extends BaseMapper<OrderVehicleSnapshotPo> {

    /**
     * 根据订单业务 ID 查询快照
     *
     * @param orderId 订单业务 ID
     * @return 订单车型配置快照 PO
     */
    OrderVehicleSnapshotPo selectByOrderId(@Param("orderId") String orderId);

}
