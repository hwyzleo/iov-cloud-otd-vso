package net.hwyz.iov.cloud.otd.vso.api.feign.mp;

import net.hwyz.iov.cloud.otd.vso.api.contract.Order;
import net.hwyz.iov.cloud.otd.vso.api.contract.request.EarnestMoneyOrderRequest;
import net.hwyz.iov.cloud.otd.vso.api.contract.request.OrderPaymentRequest;
import net.hwyz.iov.cloud.otd.vso.api.contract.request.SelectedSaleModelRequest;
import net.hwyz.iov.cloud.otd.vso.api.contract.response.OrderPaymentResponse;
import net.hwyz.iov.cloud.otd.vso.api.contract.response.OrderResponse;
import net.hwyz.iov.cloud.otd.vso.api.contract.response.WishlistResponse;
import net.hwyz.iov.cloud.tsp.framework.commons.bean.ClientAccount;
import net.hwyz.iov.cloud.tsp.framework.commons.bean.Response;

import java.util.List;

/**
 * 车辆销售订单相关手机接口
 *
 * @author hwyz_leo
 */
public interface VehicleSaleOrderMpApi {

    /**
     * 获取订单列表
     *
     * @param type          订单类型
     * @param clientAccount 终端用户
     * @return 订单列表
     */
    Response<List<Order>> getOrderList(String type, ClientAccount clientAccount);

    /**
     * 新建心愿单
     *
     * @param request       新建心愿单请求
     * @param clientAccount 终端用户
     * @return 订单编号
     */
    Response<String> createWishlist(SelectedSaleModelRequest request, ClientAccount clientAccount);

    /**
     * 修改心愿单
     *
     * @param request       修改心愿单请求
     * @param clientAccount 终端用户
     * @return 订单编号
     */
    Response<Void> modifyWishlist(SelectedSaleModelRequest request, ClientAccount clientAccount);

    /**
     * 删除心愿单
     *
     * @param request       删除心愿单请求
     * @param clientAccount 终端用户
     * @return 操作结果
     */
    Response<Void> deleteWishlist(SelectedSaleModelRequest request, ClientAccount clientAccount);

    /**
     * 获取心愿单详情
     *
     * @param orderNum      订单编号
     * @param clientAccount 终端用户
     * @return 心愿单详情
     */
    Response<WishlistResponse> getWishlist(String orderNum, ClientAccount clientAccount);

    /**
     * 意向金（小定）下订单
     *
     * @param request       意向金下单请求
     * @param clientAccount 终端用户
     * @return 订单编号
     */
    Response<String> earnestMoneyOrder(EarnestMoneyOrderRequest request, ClientAccount clientAccount);

    /**
     * 获取订单详情
     *
     * @param orderNum      订单编号
     * @param clientAccount 终端用户
     * @return 订单详情
     */
    Response<OrderResponse> getOrder(String orderNum, ClientAccount clientAccount);

    /**
     * 取消订单
     *
     * @param order         订单对象
     * @param clientAccount 终端用户
     * @return 操作结果
     */
    Response<Void> cancelOrder(Order order, ClientAccount clientAccount);

    /**
     * 支付订单
     *
     * @param request       支付订单请求
     * @param clientAccount 终端用户
     * @return 操作结果
     */
    Response<OrderPaymentResponse> payOrder(OrderPaymentRequest request, ClientAccount clientAccount);

}
