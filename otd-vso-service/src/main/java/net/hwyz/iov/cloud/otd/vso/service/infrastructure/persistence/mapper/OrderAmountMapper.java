package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import net.hwyz.iov.cloud.framework.mysql.dao.BaseDao;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderAmountPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 订单金额 Mapper 接口
 *
 * @author VSO Team
 */
@Mapper
public interface OrderAmountMapper extends BaseDao<OrderAmountPo, Long> {

    /**
     * 根据订单业务 ID 查询金额信息
     *
     * @param orderId 订单业务 ID
     * @return 订单金额 PO
     */
    OrderAmountPo selectByOrderId(@Param("orderId") String orderId);

    OrderAmountPo selectPoById(Long id);

    List<OrderAmountPo> selectPoByMap(@Param("params") Map<String, Object> params);

    int countPoByMap(@Param("params") Map<String, Object> params);

    int insertPo(OrderAmountPo entity);

    int batchInsertPo(List<OrderAmountPo> entities);

    int updatePo(OrderAmountPo entity);

    int logicalDeletePo(Long id);

    int physicalDeletePo(Long id);

    int batchPhysicalDeletePo(@Param("array") Long[] ids);

    List<OrderAmountPo> selectPoByExample(OrderAmountPo example);

}
