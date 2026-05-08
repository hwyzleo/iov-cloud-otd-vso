package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import net.hwyz.iov.cloud.framework.mysql.dao.BaseDao;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderVersionPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 订单版本 Mapper 接口
 */
@Mapper
public interface OrderVersionMapper extends BaseDao<OrderVersionPo, Long> {

    List<OrderVersionPo> selectByOrderId(@Param("orderId") String orderId);

    OrderVersionPo selectPoById(Long id);

    List<OrderVersionPo> selectPoByMap(@Param("params") Map<String, Object> params);

    int countPoByMap(@Param("params") Map<String, Object> params);

    int insertPo(OrderVersionPo entity);

    int batchInsertPo(List<OrderVersionPo> entities);

    int updatePo(OrderVersionPo entity);

    int logicalDeletePo(Long id);

    int physicalDeletePo(Long id);

    int batchPhysicalDeletePo(@Param("array") Long[] ids);

    List<OrderVersionPo> selectPoByExample(OrderVersionPo example);

}
