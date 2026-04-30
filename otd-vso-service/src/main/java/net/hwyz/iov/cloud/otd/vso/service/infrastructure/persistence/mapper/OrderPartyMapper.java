package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderPartyPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 订单客户 Mapper 接口
 *
 * @author VSO Team
 */
@Mapper
public interface OrderPartyMapper extends BaseMapper<OrderPartyPo> {

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

}
