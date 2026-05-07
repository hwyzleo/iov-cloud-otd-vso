package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import net.hwyz.iov.cloud.framework.mysql.dao.BaseDao;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 订单 Mapper 接口
 *
 * @author VSO Team
 */
@Mapper
public interface OrderMapper extends BaseDao<OrderPo, Long> {

    /**
     * 根据订单业务 ID 查询订单
     *
     * @param orderId 订单业务 ID
     * @return 订单 PO
     */
    OrderPo selectByOrderId(@Param("orderId") String orderId);

    /**
     * 根据订单号查询订单
     *
     * @param orderNo 订单号
     * @return 订单 PO
     */
    OrderPo selectByOrderNo(@Param("orderNo") String orderNo);

    /**
     * 根据小订单号查询订单
     *
     * @param smallOrderNo 小订单号
     * @return 订单 PO
     */
    OrderPo selectBySmallOrderNo(@Param("smallOrderNo") String smallOrderNo);

    /**
     * 逻辑删除订单
     *
     * @param orderNo 订单号
     * @return 影响行数
     */
    int logicalDeletePoByOrderNum(@Param("orderNo") String orderNo);

    /**
     * 物理删除订单
     *
     * @param orderNo 订单号
     * @return 影响行数
     */
    int physicalDeletePoByOrderNum(@Param("orderNo") String orderNo);

    /**
     * 根据 Map 对象获取对应数据列表
     *
     * @param params 查询参数
     * @return 订单 PO 列表
     */
    java.util.List<OrderPo> selectPoByMap(java.util.Map<String, Object> params);

    /**
     * 根据 Map 对象统计记录数
     *
     * @param params 查询参数
     * @return 记录数
     */
    int countPoByMap(java.util.Map<String, Object> params);

    /**
     * 插入订单
     *
     * @param orderPo 订单 PO
     * @return 影响行数
     */
    int insertPo(OrderPo orderPo);

    /**
     * 更新订单
     *
     * @param orderPo 订单 PO
     * @return 影响行数
     */
    int updatePo(OrderPo orderPo);

    /**
     * 根据订单号查询订单
     *
     * @param orderNo   订单号
     * @param accountId 账号ID
     * @return 订单 PO
     */
    OrderPo selectByOrderNoAndAccountId(@Param("orderNo") String orderNo, @Param("accountId") String accountId);
}
