package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.VehicleOccupancyConfigPo;
import org.apache.ibatis.annotations.Mapper;

/**
 * VIN占用有效期配置Mapper
 */
@Mapper
public interface VehicleOccupancyConfigMapper extends BaseMapper<VehicleOccupancyConfigPo> {

}