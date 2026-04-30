package net.hwyz.iov.cloud.otd.vso.service.domain.factory;

import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Order;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderState;
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
     * @param mobile    手机号
     * @param saleCode  销售代码
     * @return 车辆销售订单领域对象
     */
    public Order buildFromWishlist(String accountId, String mobile, String saleCode) {
        Order order = Order.builder()
                .build();
        order.init(accountId, mobile, saleCode, OrderState.WISHLIST);
        return order;
    }

    /**
     * 基于意向金创建车辆销售订单领域对象
     *
     * @param accountId 账号ID
     * @param mobile    手机号
     * @param saleCode  销售代码
     * @return 车辆销售订单领域对象
     */
    public Order buildFromEarnestMoney(String accountId, String mobile, String saleCode) {
        Order order = Order.builder()
                .build();
        order.init(accountId, mobile, saleCode, OrderState.EARNEST_MONEY_UNPAID);
        return order;
    }

    /**
     * 基于定金创建车辆销售订单领域对象
     *
     * @param accountId 账号ID
     * @param mobile    手机号
     * @param saleCode  销售代码
     * @return 车辆销售订单领域对象
     */
    public Order buildFromDownPayment(String accountId, String mobile, String saleCode) {
        Order order = Order.builder()
                .build();
        order.init(accountId, mobile, saleCode, OrderState.DOWN_PAYMENT_UNPAID);
        return order;
    }

}
