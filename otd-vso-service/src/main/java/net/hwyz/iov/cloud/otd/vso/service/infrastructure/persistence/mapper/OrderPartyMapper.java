package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import net.hwyz.iov.cloud.framework.mysql.dao.BaseDao;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderPartyPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 订单客户 Mapper 接口
 *
 * @author VSO Team
 */
@Mapper
public interface OrderPartyMapper extends BaseDao<OrderPartyPo, Long> {

    /**
     * 根据订单业务 ID 和角色查询
     *
     * @param orderId 订单业务 ID
     * @param partyRole 角色
     * @return 订单客户 PO
     */
    OrderPartyPo selectByOrderIdAndRole(@Param("orderId") String orderId, @Param("partyRole") String partyRole);

    /**
     * 根据订单业务 ID 查询所有角色
     *
     * @param orderId 订单业务 ID
     * @return 订单客户 PO 列表
     */
    List<OrderPartyPo> selectByOrderId(@Param("orderId") String orderId);

    OrderPartyPo selectPoById(Long id);

    List<OrderPartyPo> selectPoByMap(@Param("params") Map<String, Object> params);

    int countPoByMap(@Param("params") Map<String, Object> params);

    int insertPo(OrderPartyPo entity);

    int batchInsertPo(List<OrderPartyPo> entities);

    int updatePo(OrderPartyPo entity);

    int logicalDeletePo(Long id);

    int physicalDeletePo(Long id);

    int batchPhysicalDeletePo(@Param("array") Long[] ids);

    List<OrderPartyPo> selectPoByExample(OrderPartyPo example);

}
