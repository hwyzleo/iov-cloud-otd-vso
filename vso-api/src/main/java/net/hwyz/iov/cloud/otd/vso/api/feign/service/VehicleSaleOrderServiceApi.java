package net.hwyz.iov.cloud.otd.vso.api.feign.service;

import net.hwyz.iov.cloud.otd.vso.api.contract.Order;

/**
 * 车辆销售订单相关服务接口
 *
 * @author hwyz_leo
 */
public interface VehicleSaleOrderServiceApi {

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
