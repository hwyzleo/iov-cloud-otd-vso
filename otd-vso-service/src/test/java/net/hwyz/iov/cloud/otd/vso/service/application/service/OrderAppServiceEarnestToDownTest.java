package net.hwyz.iov.cloud.otd.vso.service.application.service;

import net.hwyz.iov.cloud.otd.vso.api.enums.OrderType;
import net.hwyz.iov.cloud.otd.vso.api.enums.SupplementaryPaymentScene;
import net.hwyz.iov.cloud.otd.vso.api.enums.SupplementaryPaymentStatus;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.EarnestToDownCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.EarnestToDownResult;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Order;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderAmount;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderState;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.shared.Money;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.*;
import net.hwyz.iov.cloud.otd.vso.service.domain.service.OrderLockService;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SupplementaryPaymentPo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderAppServiceEarnestToDownTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private SupplementaryPaymentRepository supplementaryPaymentRepository;

    @Mock
    private OrderLockService orderLockService;

    @Mock
    private AuditRepository auditRepository;

    @Mock
    private OrderPartyRepository orderPartyRepository;

    @Mock
    private OrderAssignmentRepository orderAssignmentRepository;

    @InjectMocks
    private OrderAppService orderAppService;

    private Order order;
    private EarnestToDownCmd cmd;

    @BeforeEach
    void setUp() {
        order = createOrderWithState(OrderState.EARNEST_MONEY_PAID, OrderType.SMALL);

        cmd = EarnestToDownCmd.builder()
            .accountId("test-account")
            .orderNo("TEST-001")
            .build();

        lenient().when(orderRepository.findByOrderNoAndAccountId(anyString(), anyString())).thenReturn(Optional.of(order));
        lenient().when(orderLockService.executeWithLock(anyString(), anyString(), anyString(), any(Supplier.class)))
            .thenAnswer(invocation -> {
                Supplier<?> action = invocation.getArgument(3);
                return action.get();
            });
    }

    @Test
    void shouldDirectConvertWhenDifferenceIsZero() {
        // Given: 差额 = 0
        order.getOrderAmount().setDownPaymentAmount(new Money(BigDecimal.valueOf(10000)));
        order.getOrderAmount().setDepositAmount(new Money(BigDecimal.valueOf(10000)));

        // When
        EarnestToDownResult result = orderAppService.earnestMoneyToDownPayment(cmd);

        // Then
        assertEquals(OrderType.FORMAL, result.getOrderType());
        assertEquals(OrderState.DOWN_PAYMENT_PAID, result.getOrderState());
        assertNull(result.getSupplementaryPayment());
        verify(orderRepository, atLeastOnce()).save(order);
    }

    @Test
    void shouldCreateSupplementaryPaymentWhenDifferenceIsPositive() {
        // Given: 差额 = 5000
        order.getOrderAmount().setDownPaymentAmount(new Money(BigDecimal.valueOf(15000)));
        order.getOrderAmount().setDepositAmount(new Money(BigDecimal.valueOf(10000)));

        // When
        EarnestToDownResult result = orderAppService.earnestMoneyToDownPayment(cmd);

        // Then
        assertNotNull(result.getSupplementaryPayment());
        assertTrue(result.getSupplementaryPayment().getAmount().getAmount().compareTo(new BigDecimal("5000.00")) == 0);
        // 订单状态应保持不变
        assertEquals(OrderState.EARNEST_MONEY_PAID, result.getOrderState());
        assertEquals(OrderType.SMALL, result.getOrderType());

        // 验证补款任务已保存
        ArgumentCaptor<SupplementaryPaymentPo> captor = ArgumentCaptor.forClass(SupplementaryPaymentPo.class);
        verify(supplementaryPaymentRepository).save(captor.capture());
        SupplementaryPaymentPo savedPo = captor.getValue();
        assertEquals(SupplementaryPaymentScene.EARNEST_TO_DOWN.getValue(), savedPo.getSupplementaryScene());
        assertEquals(SupplementaryPaymentStatus.PENDING.getValue(), savedPo.getSupplementaryStatus());
    }

    @Test
    void shouldDirectConvertWhenDifferenceIsNegative() {
        // Given: 差额 = -2000 (意向金 > 定金)
        order.getOrderAmount().setDownPaymentAmount(new Money(BigDecimal.valueOf(8000)));
        order.getOrderAmount().setDepositAmount(new Money(BigDecimal.valueOf(10000)));

        // When
        EarnestToDownResult result = orderAppService.earnestMoneyToDownPayment(cmd);

        // Then
        assertEquals(OrderType.FORMAL, result.getOrderType());
        assertEquals(OrderState.DOWN_PAYMENT_PAID, result.getOrderState());
        assertNull(result.getSupplementaryPayment());
    }

    @Test
    void shouldReturnIdempotentResultWhenAlreadyConverted() {
        // Given: 订单已转换
        Order convertedOrder = createOrderWithState(OrderState.DOWN_PAYMENT_PAID, OrderType.FORMAL);
        when(orderRepository.findByOrderNoAndAccountId(anyString(), anyString())).thenReturn(Optional.of(convertedOrder));

        // When
        EarnestToDownResult result = orderAppService.earnestMoneyToDownPayment(cmd);

        // Then
        assertEquals(OrderType.FORMAL, result.getOrderType());
        assertEquals(OrderState.DOWN_PAYMENT_PAID, result.getOrderState());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenStateNotEarnestMoneyPaid() {
        // Given: 状态不是EARNEST_MONEY_PAID
        Order wrongStateOrder = createOrderWithState(OrderState.DOWN_PAYMENT_PAID, OrderType.SMALL);
        when(orderRepository.findByOrderNoAndAccountId(anyString(), anyString())).thenReturn(Optional.of(wrongStateOrder));

        // When & Then
        assertThrows(Exception.class, () -> {
            orderAppService.earnestMoneyToDownPayment(cmd);
        });
    }

    private Order createOrderWithState(OrderState state, OrderType type) {
        OrderAmount orderAmount = new OrderAmount("AMT-001");
        return Order.builder()
            .id("ORDER-001")
            .orderNo("TEST-001")
            .orderState(state)
            .orderType(type)
            .orderAmount(orderAmount)
            .build();
    }
}
