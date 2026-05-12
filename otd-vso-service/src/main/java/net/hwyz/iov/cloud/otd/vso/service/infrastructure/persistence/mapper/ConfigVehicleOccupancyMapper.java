package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import net.hwyz.iov.cloud.framework.mysql.dao.BaseDao;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.ConfigVehicleOccupancyPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 车源占用有效期配置表 Mapper 接口
 */
@Mapper
public interface ConfigVehicleOccupancyMapper extends BaseDao<ConfigVehicleOccupancyPo, Long> {

    ConfigVehicleOccupancyPo selectPoById(Long id);

    List<ConfigVehicleOccupancyPo> selectPoByMap(@Param("params") Map<String, Object> params);

    int countPoByMap(@Param("params") Map<String, Object> params);

    int insertPo(ConfigVehicleOccupancyPo entity);

    int batchInsertPo(List<ConfigVehicleOccupancyPo> entities);

    int updatePo(ConfigVehicleOccupancyPo entity);

    int logicalDeletePo(Long id);

    int physicalDeletePo(Long id);

    List<ConfigVehicleOccupancyPo> selectPoByExample(ConfigVehicleOccupancyPo example);

}
