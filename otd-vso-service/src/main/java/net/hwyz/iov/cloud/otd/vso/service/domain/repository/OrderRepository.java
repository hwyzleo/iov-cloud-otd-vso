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
     * 根据小订单号查询订单
     *
     * @param smallOrderNo 小订单号
     * @return 订单
     */
    Optional<Order> findBySmallOrderNo(String smallOrderNo);

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
     * 统计订单数量
     *
     * @param deliveryPersonId 交付人员ID
     * @param delivered        是否交付
     * @return 统计数量
     */
    Integer count(String deliveryPersonId, Boolean delivered);

    /**
     * 根据订单号和账号ID查询订单
     *
     * @param accountId 账号ID
     * @param orderNo  订单号
     * @return 订单
     */
    Optional<Order> findByOrderNoAndAccountId(String orderNo, String accountId);

    List<Order> findByAccountId(String accountId, String type);

}
