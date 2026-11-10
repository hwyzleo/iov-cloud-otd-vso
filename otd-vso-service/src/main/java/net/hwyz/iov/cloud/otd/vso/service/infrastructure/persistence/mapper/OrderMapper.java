package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import net.hwyz.iov.cloud.framework.mysql.dao.BaseDao;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.DeliveryPersonCountPo;
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
     * 逻辑删除订单
     *
     * @param orderNo 订单号
     * @return 影响行数
     */
    int logicalDeletePoByOrderNo(@Param("orderNo") String orderNo);

    /**
     * 物理删除订单
     *
     * @param orderNo 订单号
     * @return 影响行数
     */
    int physicalDeletePoByOrderNo(@Param("orderNo") String orderNo);

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
     * 批量统计多个交付人员的未交付订单数（交付人员分配列表用，避免逐个 count）
     *
     * @param deliveryPersonIds 交付人员 ID 集合
     * @return 交付人员 ID -> 未交付订单数
     */
    java.util.List<DeliveryPersonCountPo> selectUndeliveredCountByDeliveryPersonIds(
            @Param("deliveryPersonIds") java.util.List<String> deliveryPersonIds);

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

    /**
     * 根据订单业务 ID 和账号查询订单（用户隔离）
     */
    OrderPo selectByOrderIdAndAccountId(@Param("orderId") String orderId, @Param("accountId") String accountId);

    /**
     * C 端按账号查询订单列表
     */
    java.util.List<OrderPo> selectByAccountId(@Param("accountId") String accountId,
                                              @Param("orderStateRange") java.util.List<Integer> orderStateRange);

    /**
     * 自动锁单到期订单查询
     */
    java.util.List<OrderPo> selectAutoLockCandidates(@Param("orderState") Integer orderState,
                                                     @Param("deadline") java.time.LocalDateTime deadline,
                                                     @Param("limit") int limit);

    /**
     * 条件状态更新（防止覆盖冲突终态）
     */
    int updateDownPaymentTime(@Param("orderNo") String orderNo,
                              @Param("downPaymentTime") java.time.LocalDateTime downPaymentTime);

    int updateOrderStateConditional(@Param("orderNo") String orderNo,
                                    @Param("targetState") Integer targetState,
                                    @Param("expectedStates") java.util.List<Integer> expectedStates);

    int existsUnpaidOrderByUserId(@Param("userId") String userId);

    int existsUnpaidOrderByMobileHash(@Param("mobileHash") String mobileHash);
}
