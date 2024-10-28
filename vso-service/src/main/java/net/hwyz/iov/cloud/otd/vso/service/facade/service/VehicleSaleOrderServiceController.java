package net.hwyz.iov.cloud.otd.vso.service.facade.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.otd.vso.api.contract.Order;
import net.hwyz.iov.cloud.otd.vso.api.feign.service.VehicleSaleOrderServiceApi;
import net.hwyz.iov.cloud.otd.vso.service.application.service.VehicleSaleOrderAppService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 车辆销售订单相关服务接口实现类
 *
 * @author hwyz_leo
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/service/vehicleSaleOrder")
public class VehicleSaleOrderServiceController implements VehicleSaleOrderServiceApi {

    private final VehicleSaleOrderAppService vehicleSaleOrderAppService;

    /**
     * 准备运输
     *
     * @param order 订单对象
     */
    @Override
    @PostMapping("/order/action/prepareTransport")
    public void prepareTransport(@RequestBody @Valid Order order) {
        logger.info("外部服务触发订单[{}]准备运输", order.getOrderNum());
        vehicleSaleOrderAppService.prepareTransport(order.getOrderNum());
    }

    /**
     * 运输中
     *
     * @param order 订单对象
     */
    @Override
    @PostMapping("/order/action/transporting")
    public void transporting(@RequestBody @Valid Order order) {
        logger.info("外部服务触发订单[{}]运输中", order.getOrderNum());
        vehicleSaleOrderAppService.transporting(order.getOrderNum());
    }

    /**
     * 待交付
     *
     * @param order 订单对象
     */
    @Override
    @PostMapping("/order/action/prepareDelivery")
    public void prepareDelivery(@RequestBody @Valid Order order) {
        logger.info("外部服务触发订单[{}]待交付", order.getOrderNum());
        vehicleSaleOrderAppService.prepareDelivery(order.getOrderNum());
    }

    /**
     * 已交付
     *
     * @param order 订单对象
     */
    @Override
    @PostMapping("/order/action/delivered")
    public void delivered(@RequestBody @Valid Order order) {
        logger.info("外部服务触发订单[{}]已交付", order.getOrderNum());
        vehicleSaleOrderAppService.delivered(order.getOrderNum());
    }

    /**
     * 激活车辆
     *
     * @param order 订单对象
     */
    @Override
    @PostMapping("/order/action/activate")
    public void activate(@RequestBody @Valid Order order) {
        logger.info("外部服务触发订单[{}]已激活", order.getOrderNum());
        vehicleSaleOrderAppService.activate(order.getOrderNum());
    }

}
