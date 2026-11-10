package net.hwyz.iov.cloud.otd.vso.service.domain.repository;

import net.hwyz.iov.cloud.otd.vso.service.domain.model.Order;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderState;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * 订单仓储接口
 *
 * @author VSO Team
 */
public interface OrderRepository {

    /**
     * 保存订单
     *
     * @param order 订单
     * @return 保存后的订单
     */
    Order save(Order order);

    /**
     * 根据订单业务 ID 查询订单
     *
     * @param orderId 订单业务 ID
     * @return 订单
     */
    Optional<Order> findByOrderId(String orderId);

    /**
     * 根据订单号查询订单
     *
     * @param orderNo 订单号
     * @return 订单
     */
    Optional<Order> findByOrderNo(String orderNo);

    /**
     * 根据订单号删除订单（软删除）
     *
     * @param orderNo 订单號
     */
    void deleteByOrderNo(String orderNo);

    /**
     * 查询订单列表（用于分页查询）
     *
     * @param orderNo          订单号
     * @param orderState        订单状态
     * @param orderStateRange   订单状态范围
     * @param hasDeliveryPerson 是否有交付人员
     * @param beginTime         开始时间
     * @param endTime           结束时间
     * @return 订单列表
     */
    List<Order> search(String orderNo, OrderState orderState, List<OrderState> orderStateRange,
                        Boolean hasDeliveryPerson, Date beginTime, Date endTime);

    /**
     * 带账号隔离的订单查询（accountId 为 null 时等价于 MPT 查询）
     */
    List<Order> searchByAccount(String accountId, String orderNo, OrderState orderState,
                                List<OrderState> orderStateRange, Boolean hasDeliveryPerson,
                                Date beginTime, Date endTime);

    /**
     * 查询账号下的订单列表（数据库层 EXISTS 隔离）
     */
    List<Order> findByAccountId(String accountId, List<OrderState> orderStateRange);

    /**
     * 查询定金支付时间早于或等于截止时间的定金已支付订单（自动锁单补偿扫描）
     */
    List<Order> findDownPaymentPaidBefore(Date deadline, int limit);

    /**
     * 条件状态更新：仅当当前状态在允许集合内才更新
     *
     * @return 影响行数，0 表示状态已变化
     */
    int updateStateConditional(String orderNo, OrderState targetState, List<OrderState> expectedStates);

    /**
     * 更新定金支付完成时间（自动锁单基准；用于补偿与数据修复）
     */
    int updateDownPaymentTime(String orderNo, Date downPaymentTime);

    /**
     * 统计订单数量
     *
     * @param deliveryPersonId 交付人员ID
     * @param delivered        是否交付
     * @return 统计数量
     */
    Integer count(String deliveryPersonId, Boolean delivered);

    /**
     * 批量统计多个交付人员的未交付订单数（避免逐个 count）
     *
     * @return 交付人员 ID -> 未交付订单数
     */
    java.util.Map<String, Integer> countUndeliveredByDeliveryPersonIds(java.util.List<String> deliveryPersonIds);

    /**
     * 根据订单号和账号ID查询订单
     *
     * @param accountId 账号ID
     * @param orderNo  订单号
     * @return 订单
     */
    Optional<Order> findByOrderNoAndAccountId(String orderNo, String accountId);

    /**
     * 根据订单业务 ID 和账号查询订单（用户隔离）
     */
    Optional<Order> findByOrderIdAndAccountId(String orderId, String accountId);

    boolean existsUnpaidOrderByUserId(String userId);

    boolean existsUnpaidOrderByMobileHash(String mobileHash);

    /**
     * 检查是否存在指定销售车型的活跃订单（非终态）
     */
    boolean existsActiveOrdersBySaleModelCode(String saleModelCode);

}
