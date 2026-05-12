package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import net.hwyz.iov.cloud.framework.mysql.dao.BaseDao;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderLockPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 订单锁 Mapper 接口
 */
@Mapper
public interface OrderLockMapper extends BaseDao<OrderLockPo, Long> {

    OrderLockPo selectByOrderIdAndScene(@Param("orderId") String orderId, @Param("lockScene") String lockScene);

    OrderLockPo selectPoById(Long id);

    List<OrderLockPo> selectPoByMap(@Param("params") Map<String, Object> params);

    int countPoByMap(@Param("params") Map<String, Object> params);

    int insertPo(OrderLockPo entity);

    int batchInsertPo(List<OrderLockPo> entities);

    int updatePo(OrderLockPo entity);

    int logicalDeletePo(Long id);

    int physicalDeletePo(Long id);

    List<OrderLockPo> selectPoByExample(OrderLockPo example);

}
