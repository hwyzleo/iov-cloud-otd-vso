package net.hwyz.iov.cloud.otd.vso.api.service;

import jakarta.validation.Valid;
import net.hwyz.iov.cloud.framework.common.bean.AjaxResult;
import net.hwyz.iov.cloud.framework.common.bean.PageResult;
import net.hwyz.iov.cloud.otd.vso.api.vo.request.BindingFactsQueryRequest;
import net.hwyz.iov.cloud.otd.vso.api.vo.request.OrderStateChangeRequest;
import net.hwyz.iov.cloud.otd.vso.api.vo.response.BindingFactsQueryResponse;
import net.hwyz.iov.cloud.otd.vso.api.vo.response.OrderDetailResponse;
import net.hwyz.iov.cloud.otd.vso.api.vo.response.TransportOrderBriefResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 订单服务接口（内部服务调用）
 * <p>
 * 契约唯一：前缀固定 {@code /api/service/order/v1}，状态回写动作统一使用 {@link OrderStateChangeRequest}，
 * 与实现类路径和入参一一对应（OTD-VSO-DSN-CR-012 / OTD-VSO-REQ-CR-012 US-028）。
 * <p>
 * 状态回写返回 {@link AjaxResult}：平台业务失败以 200 + code 表达，调用方必须校验 {@code isSuccess()}
 * 才能区分「已推进」与「被状态机拒绝」（联调发现，2026-11-18）。
 *
 * @author VSO Team
 */
@FeignClient(name = "otd-vso", path = "/api/service/order/v1")
public interface VsoOrderService {

    /**
     * 根据订单 ID 查询订单详情
     *
     * @param orderId 订单业务 ID
     * @return 订单详情
     */
    @GetMapping("/order/{orderId}")
    OrderDetailResponse getOrderById(@PathVariable("orderId") String orderId);

    /**
     * 根据订单号查询订单详情
     *
     * @param orderNo 订单号
     * @return 订单详情
     */
    @GetMapping("/order/no/{orderNo}")
    OrderDetailResponse getByOrderNo(@PathVariable("orderNo") String orderNo);

    /**
     * 查询待发运订单（补偿通道，只读）
     *
     * @param orderState         订单状态值（本期为 470 发运申请）
     * @param deliveryRegionCode 交付区域代码（可选）
     * @param pageNum            页码，从 1 开始
     * @param pageSize           每页条数，上限 500
     * @return 待发运订单分页
     */
    @GetMapping("/order/list")
    PageResult<TransportOrderBriefResponse> listTransportApplied(
            @RequestParam("orderState") Integer orderState,
            @RequestParam(value = "deliveryRegionCode", required = false) String deliveryRegionCode,
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "100") Integer pageSize);

    /**
     * 准备运输
     *
     * @param request 订单状态推进请求
     */
    @PostMapping("/order/action/prepareTransport")
    AjaxResult prepareTransport(@RequestBody @Valid OrderStateChangeRequest request);

    /**
     * 运输中
     *
     * @param request 订单状态推进请求
     */
    @PostMapping("/order/action/transporting")
    AjaxResult transporting(@RequestBody @Valid OrderStateChangeRequest request);

    /**
     * 待交付
     *
     * @param request 订单状态推进请求
     */
    @PostMapping("/order/action/prepareDelivery")
    AjaxResult prepareDelivery(@RequestBody @Valid OrderStateChangeRequest request);

    /**
     * 已交付
     *
     * @param request 订单状态推进请求
     */
    @PostMapping("/order/action/delivered")
    AjaxResult delivered(@RequestBody @Valid OrderStateChangeRequest request);

    /**
     * 查询账号与 VIN 的订单关联及交付事实（内部只读，OTD-VSO-DSN-CR-017）
     * <p>
     * 只读、可重复调用、无业务写副作用；返回最小化事实与核验状态，不是绑车资格结论。
     * 技术失败按既有技术失败语义返回，不得捕获后改成 NO_MATCH 空成功。
     *
     * @param request 账号与 VIN 查询条件
     * @return 最小化事实与核验状态
     */
    @PostMapping("/order/binding-facts/query")
    BindingFactsQueryResponse queryBindingFacts(@RequestBody @Valid BindingFactsQueryRequest request);

}
