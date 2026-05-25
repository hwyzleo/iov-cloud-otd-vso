package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import net.hwyz.iov.cloud.framework.mysql.dao.BaseDao;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.VehicleAssignmentPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 配车与车辆绑定 Mapper 接口
 */
@Mapper
public interface VehicleAssignmentMapper extends BaseDao<VehicleAssignmentPo, Long> {

    VehicleAssignmentPo selectByOrderId(@Param("orderId") String orderId);

    VehicleAssignmentPo selectPoById(Long id);

    List<VehicleAssignmentPo> selectPoByMap(@Param("params") Map<String, Object> params);

    int countPoByMap(@Param("params") Map<String, Object> params);

    int insertPo(VehicleAssignmentPo entity);

    int batchInsertPo(List<VehicleAssignmentPo> entities);

    int updatePo(VehicleAssignmentPo entity);

    int logicalDeletePo(Long id);

    int physicalDeletePo(Long id);

    List<VehicleAssignmentPo> selectPoByExample(VehicleAssignmentPo example);

    /**
     * 根据VIN查找占用记录
     */
    @Select("SELECT * FROM vso_vehicle_assignment WHERE vin = #{vin} AND assign_status IN ('ASSIGNED', 'BOUND') AND row_valid = 1 ORDER BY create_time DESC LIMIT 1")
    VehicleAssignmentPo selectOccupiedByVin(@Param("vin") String vin);

    /**
     * 查找已过期但未释放的配车记录
     */
    @Select("SELECT * FROM vso_vehicle_assignment WHERE assign_status = 'ASSIGNED' AND occupy_expire_time < NOW() AND row_valid = 1")
    List<VehicleAssignmentPo> selectExpiredAssignments();

    /**
     * 根据订单ID查找最新的有效配车记录
     */
    @Select("SELECT * FROM vso_vehicle_assignment WHERE order_id = #{orderId} AND row_valid = 1 ORDER BY create_time DESC LIMIT 1")
    VehicleAssignmentPo selectLatestByOrderId(@Param("orderId") String orderId);

}
