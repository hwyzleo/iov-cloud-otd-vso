package net.hwyz.iov.cloud.otd.vso.api.feign.mp;

import net.hwyz.iov.cloud.otd.vso.api.contract.Wishlist;
import net.hwyz.iov.cloud.otd.vso.api.contract.response.WishlistResponse;
import net.hwyz.iov.cloud.tsp.framework.commons.bean.ClientAccount;
import net.hwyz.iov.cloud.tsp.framework.commons.bean.Response;

/**
 * 车辆销售订单相关手机接口
 *
 * @author hwyz_leo
 */
public interface VehicleSaleOrderMpApi {

    /**
     * 新建心愿单
     *
     * @param request       新建心愿单请求
     * @param clientAccount 终端用户
     * @return 操作结果
     */
    Response<Void> createWishlist(Wishlist request, ClientAccount clientAccount);

    /**
     * 修改心愿单
     *
     * @param request       修改心愿单请求
     * @param clientAccount 终端用户
     * @return 操作结果
     */
    Response<Void> modifyWishlist(Wishlist request, ClientAccount clientAccount);

    /**
     * 删除心愿单
     *
     * @param clientAccount 终端用户
     * @return 操作结果
     */
    Response<Void> deleteWishlist(ClientAccount clientAccount);

    /**
     * 获取心愿单详情
     *
     * @param clientAccount 终端用户
     * @return 心愿单详情
     */
    Response<WishlistResponse> getWishlist(ClientAccount clientAccount);

}
