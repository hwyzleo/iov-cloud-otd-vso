package net.hwyz.iov.cloud.otd.vso.api.service;

import net.hwyz.iov.cloud.otd.vso.api.vo.request.Order;
import net.hwyz.iov.cloud.otd.vso.api.vo.response.OrderDetailResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 订单服务接口（内部服务调用）
 *
 * @author VSO Team
 */
@FeignClient(name = "otd-vso", path = "/api/v1/service")
public interface VsoOrderService {

    /**
     * 根据订单 ID 查询订单详情
     *
     * @param orderId 订单业务 ID
     * @return 订单详情
     */
    @GetMapping("/orders/{orderId}")
    OrderDetailResponse getOrderById(@PathVariable("orderId") String orderId);

    /**
     * 根据订单号查询订单详情
     *
     * @param orderNo 订单号
     * @return 订单详情
     */
    @GetMapping("/orders/no/{orderNo}")
    OrderDetailResponse getByOrderNo(@PathVariable("orderNo") String orderNo);

    /**
     * 准备运输
     *
     * @param order 订单对象
     */
    void prepareTransport(Order order);

    /**
     * 运输中
     *
     * @param order 订单对象
     */
    void transporting(Order order);

    /**
     * 待交付
     *
     * @param order 订单对象
     */
    void prepareDelivery(Order order);

    /**
     * 已交付
     *
     * @param order 订单对象
     */
    void delivered(Order order);

    /**
     * 激活车辆
     *
     * @param order 订单对象
     */
    void activate(Order order);

}
