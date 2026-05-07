package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.ConfigVehicleOccupancyPo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 车源占用有效期配置表 Mapper 接口
 */
@Mapper
public interface ConfigVehicleOccupancyMapper extends BaseMapper<ConfigVehicleOccupancyPo> {

}
