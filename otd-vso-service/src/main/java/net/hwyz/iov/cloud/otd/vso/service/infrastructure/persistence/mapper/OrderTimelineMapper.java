package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import net.hwyz.iov.cloud.framework.mysql.dao.BaseDao;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderTimelinePo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 订单时间线 Mapper 接口
 */
@Mapper
public interface OrderTimelineMapper extends BaseDao<OrderTimelinePo, Long> {

    List<OrderTimelinePo> selectByOrderId(@Param("orderId") String orderId);

    OrderTimelinePo selectPoById(Long id);

    List<OrderTimelinePo> selectPoByMap(@Param("params") Map<String, Object> params);

    int countPoByMap(@Param("params") Map<String, Object> params);

    int insertPo(OrderTimelinePo entity);

    int batchInsertPo(List<OrderTimelinePo> entities);

    int updatePo(OrderTimelinePo entity);

    int logicalDeletePo(Long id);

    int physicalDeletePo(Long id);

    List<OrderTimelinePo> selectPoByExample(OrderTimelinePo example);

}
