package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import net.hwyz.iov.cloud.framework.mysql.dao.BaseDao;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderAssignmentPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 订单归属 Mapper 接口
 *
 * @author VSO Team
 */
@Mapper
public interface OrderAssignmentMapper extends BaseDao<OrderAssignmentPo, Long> {

    /**
     * 根据订单业务 ID 查询归属信息
     *
     * @param orderId 订单业务 ID
     * @return 订单归属 PO
     */
    OrderAssignmentPo selectByOrderId(@Param("orderId") String orderId);

    OrderAssignmentPo selectPoById(Long id);

    List<OrderAssignmentPo> selectPoByMap(@Param("params") Map<String, Object> params);

    int countPoByMap(@Param("params") Map<String, Object> params);

    int insertPo(OrderAssignmentPo entity);

    int batchInsertPo(List<OrderAssignmentPo> entities);

    int updatePo(OrderAssignmentPo entity);

    int logicalDeletePo(Long id);

    int physicalDeletePo(Long id);

    int batchPhysicalDeletePo(@Param("array") Long[] ids);

    List<OrderAssignmentPo> selectPoByExample(OrderAssignmentPo example);

}
