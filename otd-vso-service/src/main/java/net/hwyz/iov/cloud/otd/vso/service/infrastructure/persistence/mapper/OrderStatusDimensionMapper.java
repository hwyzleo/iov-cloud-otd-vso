package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import net.hwyz.iov.cloud.framework.mysql.dao.BaseDao;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderStatusDimensionPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 订单维度状态 Mapper 接口
 *
 * @author VSO Team
 */
@Mapper
public interface OrderStatusDimensionMapper extends BaseDao<OrderStatusDimensionPo, Long> {

    /**
     * 根据订单业务 ID 查询维度状态
     *
     * @param orderId 订单业务 ID
     * @return 订单维度状态 PO
     */
    OrderStatusDimensionPo selectByOrderId(@Param("orderId") String orderId);

    OrderStatusDimensionPo selectPoById(Long id);

    List<OrderStatusDimensionPo> selectPoByMap(@Param("params") Map<String, Object> params);

    int countPoByMap(@Param("params") Map<String, Object> params);

    int insertPo(OrderStatusDimensionPo entity);

    int batchInsertPo(List<OrderStatusDimensionPo> entities);

    int updatePo(OrderStatusDimensionPo entity);

    int logicalDeletePo(Long id);

    int physicalDeletePo(Long id);

    int batchPhysicalDeletePo(@Param("array") Long[] ids);

    List<OrderStatusDimensionPo> selectPoByExample(OrderStatusDimensionPo example);

}
