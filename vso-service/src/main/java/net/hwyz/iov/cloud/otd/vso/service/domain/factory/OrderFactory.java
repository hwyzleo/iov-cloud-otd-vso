package net.hwyz.iov.cloud.otd.vso.service.domain.factory;

import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.otd.vso.service.domain.contract.enums.OrderState;
import net.hwyz.iov.cloud.otd.vso.service.domain.order.model.OrderDo;
import org.springframework.stereotype.Component;

/**
 * 车辆销售订单领域工厂类
 *
 * @author hwyz_leo
 */
@Slf4j
@Component
public class OrderFactory {

    /**
     * 基于心愿单创建车辆销售订单领域对象
     *
     * @param accountId 账号ID
     * @param saleCode  销售代码
     * @return 车辆销售订单领域对象
     */
    public OrderDo buildFromWishlist(String accountId, String saleCode) {
        OrderDo orderDo = OrderDo.builder()
                .build();
        orderDo.init(accountId, saleCode, OrderState.WISHLIST);
        return orderDo;
    }

    /**
     * 基于意向金创建车辆销售订单领域对象
     *
     * @param accountId 账号ID
     * @param saleCode  销售代码
     * @return 车辆销售订单领域对象
     */
    public OrderDo buildFromEarnestMoney(String accountId, String saleCode) {
        OrderDo orderDo = OrderDo.builder()
                .build();
        orderDo.init(accountId, saleCode, OrderState.EARNEST_MONEY_UNPAID);
        return orderDo;
    }

    /**
     * 基于定金创建车辆销售订单领域对象
     *
     * @param accountId 账号ID
     * @param saleCode  销售代码
     * @return 车辆销售订单领域对象
     */
    public OrderDo buildFromDownPayment(String accountId, String saleCode) {
        OrderDo orderDo = OrderDo.builder()
                .build();
        orderDo.init(accountId, saleCode, OrderState.DOWN_PAYMENT_UNPAID);
        return orderDo;
    }

}
