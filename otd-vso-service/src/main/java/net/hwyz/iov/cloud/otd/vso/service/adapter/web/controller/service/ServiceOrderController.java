package net.hwyz.iov.cloud.otd.vso.service.adapter.web.controller.service;

import com.github.pagehelper.PageHelper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.framework.common.bean.AjaxResult;
import net.hwyz.iov.cloud.framework.common.bean.PageResult;
import net.hwyz.iov.cloud.framework.web.controller.BaseController;
import net.hwyz.iov.cloud.framework.web.util.PageUtil;
import net.hwyz.iov.cloud.otd.vso.api.service.VsoOrderService;
import net.hwyz.iov.cloud.otd.vso.api.vo.request.OrderStateChangeRequest;
import net.hwyz.iov.cloud.otd.vso.api.vo.response.OrderDetailResponse;
import net.hwyz.iov.cloud.otd.vso.api.vo.response.TransportOrderBriefResponse;
import net.hwyz.iov.cloud.otd.vso.service.adapter.web.assembler.OrderDetailResponseAssembler;
import net.hwyz.iov.cloud.otd.vso.service.adapter.web.assembler.TransportOrderBriefAssembler;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.ActivateCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.DeliveredCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.PrepareDeliveryCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.PrepareTransportCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.TransportingCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.OrderDetailResult;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.TransportOrderBriefResult;
import net.hwyz.iov.cloud.otd.vso.service.application.service.OrderAppService;
import net.hwyz.iov.cloud.otd.vso.service.application.service.TransportFulfillmentAppService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 车辆销售订单相关服务接口实现类（内部服务调用）
 * <p>
 * 契约唯一：路径前缀固定 {@code /api/service/order/v1}，状态回写动作统一使用 {@link OrderStateChangeRequest}，
 * 与 {@code otd-vso-api} 中的 {@link VsoOrderService} 声明一一对应（OTD-VSO-DSN-CR-012）。
 *
 * @author hwyz_leo
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/service/order/v1")
public class ServiceOrderController extends BaseController implements VsoOrderService {

    /**
     * 补偿查询单页上限
     */
    private static final int MAX_PAGE_SIZE = 500;

    private final OrderAppService orderAppService;
    private final TransportFulfillmentAppService transportFulfillmentAppService;

    @Override
    @GetMapping("/order/{orderId}")
    public OrderDetailResponse getOrderById(@PathVariable("orderId") String orderId) {
        OrderDetailResult result = orderAppService.getById(orderId);
        return OrderDetailResponseAssembler.INSTANCE.toVo(result);
    }

    @Override
    @GetMapping("/order/no/{orderNo}")
    public OrderDetailResponse getByOrderNo(@PathVariable("orderNo") String orderNo) {
        OrderDetailResult result = orderAppService.getByOrderNo(orderNo);
        return OrderDetailResponseAssembler.INSTANCE.toVo(result);
    }

    @Override
    @GetMapping("/order/list")
    public PageResult<TransportOrderBriefResponse> listTransportApplied(
            @RequestParam("orderState") Integer orderState,
            @RequestParam(value = "deliveryRegionCode", required = false) String deliveryRegionCode,
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "100") Integer pageSize) {
        int page = pageNum == null || pageNum < 1 ? 1 : pageNum;
        int size = pageSize == null || pageSize < 1 ? 100 : Math.min(pageSize, MAX_PAGE_SIZE);
        PageHelper.startPage(page, size);
        try {
            List<TransportOrderBriefResult> results =
                    transportFulfillmentAppService.searchTransportApplied(orderState, deliveryRegionCode);
            return getPageResult(PageUtil.convert(results, TransportOrderBriefAssembler.INSTANCE::toVo));
        } finally {
            clearPage();
        }
    }

    @Override
    @PostMapping("/order/action/prepareTransport")
    public AjaxResult prepareTransport(@RequestBody @Valid OrderStateChangeRequest request) {
        log.info("外部服务触发订单[{}]准备运输", request.getOrderNo());
        orderAppService.prepareTransport(PrepareTransportCmd.builder().orderNo(request.getOrderNo()).build());
        return success();
    }

    @Override
    @PostMapping("/order/action/transporting")
    public AjaxResult transporting(@RequestBody @Valid OrderStateChangeRequest request) {
        log.info("外部服务触发订单[{}]运输中", request.getOrderNo());
        orderAppService.transporting(TransportingCmd.builder().orderNo(request.getOrderNo()).build());
        return success();
    }

    @Override
    @PostMapping("/order/action/prepareDelivery")
    public AjaxResult prepareDelivery(@RequestBody @Valid OrderStateChangeRequest request) {
        log.info("外部服务触发订单[{}]待交付", request.getOrderNo());
        orderAppService.prepareDelivery(PrepareDeliveryCmd.builder().orderNo(request.getOrderNo()).build());
        return success();
    }

    @Override
    @PostMapping("/order/action/delivered")
    public AjaxResult delivered(@RequestBody @Valid OrderStateChangeRequest request) {
        log.info("外部服务触发订单[{}]已交付", request.getOrderNo());
        orderAppService.delivered(DeliveredCmd.builder().orderNo(request.getOrderNo()).build());
        return success();
    }

    @Override
    @PostMapping("/order/action/activate")
    public AjaxResult activate(@RequestBody @Valid OrderStateChangeRequest request) {
        log.info("外部服务触发订单[{}]激活", request.getOrderNo());
        orderAppService.activate(ActivateCmd.builder().orderNo(request.getOrderNo()).build());
        return success();
    }
}
