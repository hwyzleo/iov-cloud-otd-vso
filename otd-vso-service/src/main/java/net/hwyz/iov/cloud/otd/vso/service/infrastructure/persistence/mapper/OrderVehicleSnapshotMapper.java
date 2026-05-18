package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import net.hwyz.iov.cloud.framework.mysql.dao.BaseDao;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderVehicleSnapshotPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 订单车型配置快照 Mapper 接口
 *
 * @author VSO Team
 */
@Mapper
public interface OrderVehicleSnapshotMapper extends BaseDao<OrderVehicleSnapshotPo, Long> {

    /**
     * 根据订单业务 ID 查询快照
     *
     * @param orderId 订单业务 ID
     * @return 订单车型配置快照 PO
     */
    OrderVehicleSnapshotPo selectByOrderId(@Param("orderId") String orderId);

    OrderVehicleSnapshotPo selectPoById(Long id);

    List<OrderVehicleSnapshotPo> selectPoByMap(@Param("params") Map<String, Object> params);

    int countPoByMap(@Param("params") Map<String, Object> params);

    int insertPo(OrderVehicleSnapshotPo entity);

    int batchInsertPo(List<OrderVehicleSnapshotPo> entities);

    int updatePo(OrderVehicleSnapshotPo entity);

    int logicalDeletePo(Long id);

    int physicalDeletePo(Long id);

    List<OrderVehicleSnapshotPo> selectPoByExample(OrderVehicleSnapshotPo example);

    Integer selectMaxVersionByOrderId(@Param("orderId") String orderId);

    int logicalDeleteByOrderId(@Param("orderId") String orderId);

}
