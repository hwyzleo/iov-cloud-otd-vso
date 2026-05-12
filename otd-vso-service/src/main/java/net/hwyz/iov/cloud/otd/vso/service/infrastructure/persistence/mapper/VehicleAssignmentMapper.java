package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import net.hwyz.iov.cloud.framework.mysql.dao.BaseDao;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.VehicleAssignmentPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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

}
