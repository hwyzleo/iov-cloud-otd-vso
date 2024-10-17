package net.hwyz.iov.cloud.otd.vso.service.facade.mp;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.otd.vso.api.contract.Order;
import net.hwyz.iov.cloud.otd.vso.api.contract.request.EarnestMoneyOrderRequest;
import net.hwyz.iov.cloud.otd.vso.api.contract.request.SelectedSaleModelRequest;
import net.hwyz.iov.cloud.otd.vso.api.contract.response.OrderResponse;
import net.hwyz.iov.cloud.otd.vso.api.contract.response.WishlistResponse;
import net.hwyz.iov.cloud.otd.vso.api.feign.mp.VehicleSaleOrderMpApi;
import net.hwyz.iov.cloud.otd.vso.service.application.service.VehicleSaleOrderAppService;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.dao.OrderDao;
import net.hwyz.iov.cloud.tsp.framework.commons.bean.ClientAccount;
import net.hwyz.iov.cloud.tsp.framework.commons.bean.Response;
import net.hwyz.iov.cloud.tsp.framework.commons.util.ParamHelper;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 车辆销售订单相关手机接口实现类
 *
 * @author hwyz_leo
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/mp/vehicleSaleOrder")
public class VehicleSaleOrderMpController implements VehicleSaleOrderMpApi {

    private final OrderDao orderDao;
    private final VehicleSaleOrderAppService vehicleSaleOrderAppService;

    /**
     * 获取订单列表
     *
     * @param clientAccount 终端用户
     * @return 订单列表
     */
    @Override
    @GetMapping("/order")
    public Response<List<Order>> getOrderList(ClientAccount clientAccount) {
        logger.info("手机客户端[{}]获取订单列表", ParamHelper.getClientAccountInfo(clientAccount));
        return new Response<>(vehicleSaleOrderAppService.getOrderList(clientAccount.getAccountId()));
    }

    /**
     * 新建心愿单
     *
     * @param request       新建心愿单请求
     * @param clientAccount 终端用户
     * @return 操作结果
     */
    @Override
    @PostMapping("/wishlist/action/create")
    public Response<String> createWishlist(@RequestBody @Valid SelectedSaleModelRequest request, @RequestHeader ClientAccount clientAccount) {
        logger.info("手机客户端[{}]新建心愿单", ParamHelper.getClientAccountInfo(clientAccount));
        return new Response<>(vehicleSaleOrderAppService.createUserWishlist(clientAccount.getAccountId(), request));
    }

    /**
     * 修改心愿单
     *
     * @param request       修改心愿单请求
     * @param clientAccount 终端用户
     * @return 操作结果
     */
    @Override
    @PostMapping("/wishlist/action/modify")
    public Response<Void> modifyWishlist(@RequestBody @Valid SelectedSaleModelRequest request, @RequestHeader ClientAccount clientAccount) {
        logger.info("手机客户端[{}]修改心愿单[{}]", ParamHelper.getClientAccountInfo(clientAccount), request.getOrderNum());
        vehicleSaleOrderAppService.modifyUserWishlist(clientAccount.getAccountId(), request);
        return new Response<>();
    }

    /**
     * 删除心愿单
     *
     * @param clientAccount 终端用户
     * @return 操作结果
     */
    @Override
    @PostMapping("/wishlist/action/delete")
    public Response<Void> deleteWishlist(@RequestBody @Valid SelectedSaleModelRequest request, @RequestHeader ClientAccount clientAccount) {
        logger.info("手机客户端[{}]删除心愿单[{}]", ParamHelper.getClientAccountInfo(clientAccount), request.getOrderNum());
        vehicleSaleOrderAppService.deleteUserWishlist(clientAccount.getAccountId(), request.getOrderNum());
        return new Response<>();
    }

    /**
     * 获取心愿单详情
     *
     * @param clientAccount 终端用户
     * @return 心愿单详情
     */
    @Override
    @GetMapping("/wishlist/{orderNum}")
    public Response<WishlistResponse> getWishlist(@PathVariable String orderNum, @RequestHeader ClientAccount clientAccount) {
        logger.info("手机客户端[{}]获取心愿单[{}]详情", ParamHelper.getClientAccountInfo(clientAccount), orderNum);
        return new Response<>(vehicleSaleOrderAppService.getUserWishlistResponse(clientAccount.getAccountId(), orderNum));
    }

    /**
     * 意向金（小定）下订单
     *
     * @param request       意向金下单请求
     * @param clientAccount 终端用户
     * @return 订单编号
     */
    @Override
    @PostMapping("/action/earnestMoneyOrder")
    public Response<String> earnestMoneyOrder(@RequestBody @Valid EarnestMoneyOrderRequest request, @RequestHeader ClientAccount clientAccount) {
        logger.info("手机客户端[{}]意向金（小定）下订单", ParamHelper.getClientAccountInfo(clientAccount));
        return new Response<>(vehicleSaleOrderAppService.earnestMoneyOrder(clientAccount.getAccountId(), request));
    }

    /**
     * 获取订单详情
     *
     * @param orderNum      订单编号
     * @param clientAccount 终端用户
     * @return 订单详情
     */
    @Override
    @GetMapping("/order/{orderNum}")
    public Response<OrderResponse> getOrder(@PathVariable String orderNum, @RequestHeader ClientAccount clientAccount) {
        logger.info("手机客户端[{}]获取订单[{}]详情", ParamHelper.getClientAccountInfo(clientAccount), orderNum);
        return new Response<>(vehicleSaleOrderAppService.getUserOrderResponse(clientAccount.getAccountId(), orderNum));
    }
}
