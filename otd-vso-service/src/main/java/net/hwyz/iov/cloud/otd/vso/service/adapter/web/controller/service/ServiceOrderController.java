package net.hwyz.iov.cloud.otd.vso.service.adapter.web.controller.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.framework.web.controller.BaseController;
import net.hwyz.iov.cloud.otd.vso.api.service.VsoOrderService;
import net.hwyz.iov.cloud.otd.vso.api.vo.request.Order;
import net.hwyz.iov.cloud.otd.vso.api.vo.response.OrderDetailResponse;
import net.hwyz.iov.cloud.otd.vso.service.adapter.web.assembler.*;
import net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo.*;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.*;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.OrderDetailResult;
import net.hwyz.iov.cloud.otd.vso.service.application.service.OrderAppService;
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
@RequestMapping(value = "/api/service/order/v1")
public class ServiceOrderController extends BaseController implements VsoOrderService {

    private final OrderAppService vehicleSaleOrderAppService;

    /**
     * 准备运输
     *
     * @param vo Vo对象
     */
    @PostMapping("/order/action/prepareTransport")
    public void prepareTransport(@RequestBody @Valid PrepareTransportVo vo) {
        log.info("外部服务触发订单[{}]准备运输", vo.getOrderNum());
        PrepareTransportCmd cmd = PrepareTransportVoAssembler.INSTANCE.toCmd(vo);
        vehicleSaleOrderAppService.prepareTransport(cmd);
    }

    /**
     * 运输中
     *
     * @param vo Vo对象
     */
    @PostMapping("/order/action/transporting")
    public void transporting(@RequestBody @Valid TransportingVo vo) {
        log.info("外部服务触发订单[{}]运输中", vo.getOrderNum());
        TransportingCmd cmd = TransportingVoAssembler.INSTANCE.toCmd(vo);
        vehicleSaleOrderAppService.transporting(cmd);
    }

    /**
     * 待交付
     *
     * @param vo Vo对象
     */
    @PostMapping("/order/action/prepareDelivery")
    public void prepareDelivery(@RequestBody @Valid PrepareDeliveryVo vo) {
        log.info("外部服务触发订单[{}]待交付", vo.getOrderNum());
        PrepareDeliveryCmd cmd = PrepareDeliveryVoAssembler.INSTANCE.toCmd(vo);
        vehicleSaleOrderAppService.prepareDelivery(cmd);
    }

    /**
     * 已交付
     *
     * @param vo Vo对象
     */
    @PostMapping("/order/action/delivered")
    public void delivered(@RequestBody @Valid DeliveredVo vo) {
        log.info("外部服务触发订单[{}]已交付", vo.getOrderNum());
        DeliveredCmd cmd = DeliveredVoAssembler.INSTANCE.toCmd(vo);
        vehicleSaleOrderAppService.delivered(cmd);
    }

    /**
     * 激活车辆
     *
     * @param vo Vo对象
     */
    @PostMapping("/order/action/activate")
    public void activate(@RequestBody @Valid ActivateVo vo) {
        log.info("外部服务触发订单[{}]已激活", vo.getOrderNum());
        ActivateCmd cmd = ActivateVoAssembler.INSTANCE.toCmd(vo);
        vehicleSaleOrderAppService.activate(cmd);
    }

    @Override
    public OrderDetailResponse getOrderById(String orderId) {
        OrderDetailResult result = vehicleSaleOrderAppService.getById(orderId);
        return OrderDetailResponseAssembler.INSTANCE.toVo(result);
    }

    @Override
    public OrderDetailResponse getByOrderNo(String orderNo) {
        OrderDetailResult result = vehicleSaleOrderAppService.getByOrderNum(orderNo);
        return OrderDetailResponseAssembler.INSTANCE.toVo(result);
    }

    @Override
    public void prepareTransport(Order order) {
        PrepareTransportCmd cmd = ServiceOrderRequestAssembler.INSTANCE.toPrepareTransportCmd(order);
        vehicleSaleOrderAppService.prepareTransport(cmd);
    }

    @Override
    public void transporting(Order order) {
        TransportingCmd cmd = ServiceOrderRequestAssembler.INSTANCE.toTransportingCmd(order);
        vehicleSaleOrderAppService.transporting(cmd);
    }

    @Override
    public void prepareDelivery(Order order) {
        PrepareDeliveryCmd cmd = ServiceOrderRequestAssembler.INSTANCE.toPrepareDeliveryCmd(order);
        vehicleSaleOrderAppService.prepareDelivery(cmd);
    }

    @Override
    public void delivered(Order order) {
        DeliveredCmd cmd = ServiceOrderRequestAssembler.INSTANCE.toDeliveredCmd(order);
        vehicleSaleOrderAppService.delivered(cmd);
    }

    @Override
    public void activate(Order order) {
        ActivateCmd cmd = ServiceOrderRequestAssembler.INSTANCE.toActivateCmd(order);
        vehicleSaleOrderAppService.activate(cmd);
    }
}
