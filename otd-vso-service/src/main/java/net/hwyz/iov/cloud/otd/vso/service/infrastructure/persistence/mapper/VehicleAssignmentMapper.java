package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.VehicleAssignmentPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 配车与车辆绑定 Mapper 接口
 */
@Mapper
public interface VehicleAssignmentMapper extends BaseMapper<VehicleAssignmentPo> {

    VehicleAssignmentPo selectByOrderId(@Param("orderId") String orderId);

}
