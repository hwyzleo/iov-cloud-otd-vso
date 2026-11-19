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

    /**
     * 统计指定订单指定事件类型的记录数（OTD-VSO-DSN-CR-005）
     */
    int countByOrderIdAndEventType(@Param("orderId") String orderId, @Param("eventType") String eventType);

    /**
     * 统计指定订单、事件类型与关联单号的事件次数（CR-014：同一订单多次支付按支付单号去重）
     */
    int countByOrderIdAndEventTypeAndRelatedDocNo(@Param("orderId") String orderId,
                                                  @Param("eventType") String eventType,
                                                  @Param("relatedDocNo") String relatedDocNo);

    OrderTimelinePo selectPoById(Long id);

    /**
     * 批量查询多个订单的最新事件时间（订单状态时间派生用）
     */
    List<OrderTimelinePo> selectLatestEventTimeByOrderIds(@Param("orderIds") List<String> orderIds);

    List<OrderTimelinePo> selectPoByMap(@Param("params") Map<String, Object> params);

    int countPoByMap(@Param("params") Map<String, Object> params);

    int insertPo(OrderTimelinePo entity);

    int batchInsertPo(List<OrderTimelinePo> entities);

    int updatePo(OrderTimelinePo entity);

    int logicalDeletePo(Long id);

    int physicalDeletePo(Long id);

    List<OrderTimelinePo> selectPoByExample(OrderTimelinePo example);

}
