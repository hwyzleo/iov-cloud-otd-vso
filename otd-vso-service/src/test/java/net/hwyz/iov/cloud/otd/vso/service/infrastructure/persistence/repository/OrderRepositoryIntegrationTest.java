package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.repository;

import net.hwyz.iov.cloud.otd.vso.api.enums.OrderType;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Order;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderAmount;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderState;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.shared.Money;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderAmountRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderPartyRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderPartyPo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 订单仓储集成测试（US-002 / INV-001 / INV-002 / US-007 数据基础）
 *
 * @author VSO Team
 */
@SpringBootTest(properties = {
        "spring.main.allow-bean-definition-overriding=true",
        "iov.kafka.topic-provisioning.enabled=false"
})
@Transactional
@DisplayName("订单仓储 - EXISTS 用户隔离/字段映射/自动锁单查询")
class OrderRepositoryIntegrationTest {

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderAmountRepository orderAmountRepository;
    @Autowired
    private OrderPartyRepository orderPartyRepository;

    @Test
    @DisplayName("双账号隔离：账号 B 无法按订单号/订单ID 查到账号 A 的订单")
    void 双账号隔离() {
        String suffix = String.valueOf(System.nanoTime());
        String accountA = "userA_" + suffix;
        String accountB = "userB_" + suffix;
        Order order = persistOrder(accountA, "SO_" + suffix);

        assertTrue(orderRepository.findByOrderNoAndAccountId(order.getOrderNo(), accountA).isPresent());
        assertFalse(orderRepository.findByOrderNoAndAccountId(order.getOrderNo(), accountB).isPresent(),
                "账号 B 不得查询账号 A 的订单");
        assertTrue(orderRepository.findByOrderIdAndAccountId(order.getId(), accountA).isPresent());
        assertFalse(orderRepository.findByOrderIdAndAccountId(order.getId(), accountB).isPresent());

        List<Order> listA = orderRepository.findByAccountId(accountA, null);
        List<Order> listB = orderRepository.findByAccountId(accountB, null);
        assertTrue(listA.stream().anyMatch(o -> o.getId().equals(order.getId())));
        assertFalse(listB.stream().anyMatch(o -> o.getId().equals(order.getId())),
                "C 端列表必须在 SQL 层按账号过滤");

        List<Order> searchB = orderRepository.searchByAccount(accountB, order.getOrderNo(), null, null,
                null, null, null);
        assertTrue(searchB.isEmpty(), "带账号的 search 不得返回他人订单");
        List<Order> searchMpt = orderRepository.searchByAccount(null, order.getOrderNo(), null, null,
                null, null, null);
        assertFalse(searchMpt.isEmpty(), "MPT 不传账号时仍可查询");
    }

    @Test
    @DisplayName("新增字段与金额标记双向映射")
    void 字段双向映射() {
        String suffix = String.valueOf(System.nanoTime());
        String account = "user_map_" + suffix;
        Order order = persistOrder(account, "SO_MAP_" + suffix);

        Order loaded = orderRepository.findByOrderId(order.getId()).orElseThrow();
        assertEquals(order.getDeliveryVin(), loaded.getDeliveryVin(), "deliveryVin 应映射到 vehicle_vin");
        assertEquals(order.getDeliveryPersonId(), loaded.getDeliveryPersonId());
        assertEquals(order.getDeliveryPersonName(), loaded.getDeliveryPersonName());
        assertEquals(Integer.valueOf(3), loaded.getPurchasePlan());
        assertNotNull(loaded.getDownPaymentTime());
        assertNotNull(loaded.getTransportApplyTime());
        assertEquals(order.getTransportApplyPersonId(), loaded.getTransportApplyPersonId());
        assertEquals(order.getTransportApplyPersonName(), loaded.getTransportApplyPersonName());

        OrderAmount amount = orderAmountRepository.findDomainByOrderId(order.getId()).orElseThrow();
        assertTrue(amount.isUpgradedFromSmall(), "upgraded_from_small 应双向映射");
        assertEquals(0, new BigDecimal("20000.00").compareTo(amount.getPaidTotal().getAmount()));
    }

    @Test
    @DisplayName("自动锁单查询：仅返回 DOWN_PAYMENT_PAID 且已到期的订单")
    void 自动锁单查询() {
        String suffix = String.valueOf(System.nanoTime());
        Order expired = persistOrder("lock_expired_" + suffix, "SO_LOCK_EXP_" + suffix);
        Order fresh = persistOrder("lock_fresh_" + suffix, "SO_LOCK_FRESH_" + suffix);
        // 未到期：定金支付时间置为未来（模拟 48 小时窗口内）
        orderRepository.updateDownPaymentTime(fresh.getOrderNo(),
                new Date(System.currentTimeMillis() + 3600_000L));

        Date deadline = new Date(System.currentTimeMillis() - 60_000L);
        List<Order> candidates = orderRepository.findDownPaymentPaidBefore(deadline, 500);

        assertTrue(candidates.stream().anyMatch(o -> o.getId().equals(expired.getId())),
                "已到期订单应被扫描到");
        assertFalse(candidates.stream().anyMatch(o -> o.getId().equals(fresh.getId())),
                "未到期订单不应被扫描到");
        assertTrue(candidates.stream().allMatch(o -> o.getOrderState() == OrderState.DOWN_PAYMENT_PAID));
    }

    private Order persistOrder(String accountId, String orderNo) {
        Order order = Order.builder()
                .id("OID_" + orderNo)
                .orderNo(orderNo)
                .orderType(OrderType.FORMAL)
                .orderSource("capp")
                .customerType("personal")
                .orderState(OrderState.DOWN_PAYMENT_PAID)
                .brandCode("OPENIOV")
                .customerType("personal")
                .orderSource("capp")
                .hasException(false)
                .currentVersionNo(1)
                .lockedFlag(false)
                .reopenFlag(false)
                .deliveryVin("VIN" + Math.abs(orderNo.hashCode()))
                .deliveryPersonId("DP_" + orderNo)
                .deliveryPersonName("交付员" + orderNo)
                .transportApplyPersonId("TA_" + orderNo)
                .transportApplyPersonName("发运员" + orderNo)
                .transportApplyTime(new Date())
                .downPaymentTime(new Date(System.currentTimeMillis() - 3600_000L))
                .purchasePlan(3)
                .build();
        OrderAmount amount = new OrderAmount("AMT_" + orderNo);
        amount.setVehiclePrice(new Money(new BigDecimal("200000.00")));
        amount.setDepositAmount(new Money(new BigDecimal("5000.00")));
        amount.setDownPaymentAmount(new Money(new BigDecimal("20000.00")));
        amount.setPaidTotal(new Money(new BigDecimal("20000.00")));
        amount.setUpgradedFromSmall(true);
        amount.recalculate();
        order.setOrderAmount(amount);
        orderRepository.save(order);

        OrderPartyPo party = new OrderPartyPo();
        party.setPartyId("PID_" + orderNo);
        party.setOrderId(order.getId());
        party.setPartyRole("order_user");
        party.setUserId(accountId);
        party.setAuthorizedFlag(0);
        orderPartyRepository.save(party);
        return order;
    }
}
