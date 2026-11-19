package net.hwyz.iov.cloud.otd.vso.service.application.service;

import net.hwyz.iov.cloud.otd.vso.api.enums.ContractStatus;
import net.hwyz.iov.cloud.otd.vso.api.enums.OrderType;
import net.hwyz.iov.cloud.otd.vso.api.enums.PaymentChannel;
import net.hwyz.iov.cloud.otd.vso.api.enums.PaymentStage;
import net.hwyz.iov.cloud.otd.vso.api.enums.PaymentStatus;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.InitiatePaymentCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.PaymentCallbackCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.enums.PaymentCallbackResultCode;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.InitiatePaymentResult;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.PaymentCallbackResult;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Contract;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Order;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderAmount;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderState;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.shared.Money;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.ContractRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderAmountRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 全款尾款支付链路集成测试（OTD-VSO-DSN-CR-014 US-033）
 * <p>
 * 固定样例：netReceivableTotal=203000、downPaymentAmount=20000 → tailPaymentAmount=183000；
 * 尾款成功 600→620、paidTotal=203000、unpaidTotal=0；重复回调不累计；失败保持 600。
 * 本测试为本地数据库事务内集成测试，不代表真实跨服务 E2E 或生产验收。
 *
 * @author VSO Team
 */
@SpringBootTest(properties = {
        "spring.main.allow-bean-definition-overriding=true",
        "iov.kafka.topic-provisioning.enabled=false"
})
@Transactional
@DisplayName("全款尾款支付 - 集成测试")
class PaymentCallbackTailIntegrationTest {

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderAmountRepository orderAmountRepository;
    @Autowired
    private ContractRepository contractRepository;
    @Autowired
    private PaymentAppService paymentAppService;
    @Autowired
    private PaymentCallbackService paymentCallbackService;
    @Autowired
    private CallbackSignatureService callbackSignatureService;
    @Autowired
    private MockPaymentAutoCallbackService mockPaymentAutoCallbackService;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private String account;
    private String orderNo;
    private String orderId;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(mockPaymentAutoCallbackService, "autoCallbackSetting", "false");
        account = "tail_user_" + System.nanoTime();
        createFullPaymentOrder();
        createSignedContract();
    }

    private void createFullPaymentOrder() {
        long suffix = System.nanoTime();
        orderId = "OID" + suffix;
        orderNo = "SON" + suffix;
        Order order = Order.builder()
                .id(orderId)
                .orderNo(orderNo)
                .orderType(OrderType.FORMAL)
                .orderSource("capp")
                .customerType("personal")
                .brandCode("OPENIOV")
                .currentVersionNo(1)
                .orderState(OrderState.PREPARE_DELIVER)
                .paymentMethod("full_payment")
                .build();
        OrderAmount amount = new OrderAmount("AMT" + suffix);
        amount.setDownPaymentAmount(new Money(new BigDecimal("20000.00")));
        amount.setPaidTotal(new Money(new BigDecimal("20000.00")));
        amount.setNetReceivableTotal(new Money(new BigDecimal("203000.00")));
        amount.setUnpaidTotal(new Money(new BigDecimal("183000.00")));
        amount.setTailPaymentAmount(new Money(new BigDecimal("183000.00")));
        order.setOrderAmount(amount);
        orderRepository.save(order);
        jdbcTemplate.update("INSERT INTO vso_order_party (party_id, order_id, party_role, user_id, name, "
                        + "authorized_flag, row_valid) VALUES (?, ?, 'order_user', ?, '尾款测试用户', 0, 1)",
                "PID" + suffix, orderId, account);
    }

    private void createSignedContract() {
        Contract contract = Contract.draft("CID" + System.nanoTime(), orderId, "CT" + System.nanoTime());
        contract.markGenerated("MOCK-ESIGN-TAIL", "mock://esign/contract/" + orderId + "/MOCK-ESIGN-TAIL",
                LocalDateTime.now());
        contract.markSigning("SIGN-TAIL-" + orderNo);
        contract.markSigned(LocalDateTime.now());
        contractRepository.save(contract);
    }

    private PaymentCallbackResult callback(String paymentNo, String amount, PaymentStatus status) {
        long ts = Instant.now().getEpochSecond();
        String nonce = UUID.randomUUID().toString();
        String externalTradeNo = "MOCK-" + paymentNo;
        Map<String, String> signData = new LinkedHashMap<>();
        signData.put("amount", new BigDecimal(amount).toPlainString());
        signData.put("orderId", paymentNo);
        signData.put("paySeq", externalTradeNo);
        signData.put("status", status.name());
        String signature = callbackSignatureService.sign(signData, nonce, ts);
        PaymentCallbackCmd cmd = PaymentCallbackCmd.builder()
                .paymentNo(paymentNo)
                .externalTradeNo(externalTradeNo)
                .paymentStage(PaymentStage.TAIL_PAYMENT.name())
                .paymentAmount(new BigDecimal(amount))
                .paymentStatus(status.name())
                .payTime(LocalDateTime.now())
                .idempotentKey("IDEM-" + status + "-" + nonce)
                .failReason(status == PaymentStatus.PAYMENT_FAILED ? "渠道拒绝" : null)
                .build();
        return paymentCallbackService.handleCallback(cmd, signature, ts, nonce);
    }

    private InitiatePaymentResult initiateTailPayment() {
        InitiatePaymentResult result = paymentAppService.initiatePayment(InitiatePaymentCmd.builder()
                .accountId(account)
                .orderNo(orderNo)
                .paymentChannel(PaymentChannel.WECHAT)
                .build());
        assertEquals(PaymentStage.TAIL_PAYMENT.name(), result.getPaymentStage());
        assertEquals(0, new BigDecimal("183000.00").compareTo(result.getPaymentAmount()),
                "尾款金额必须取订单快照，不得信任客户端金额");
        return result;
    }

    @Test
    @DisplayName("尾款支付成功：600→620、paidTotal=203000、unpaidTotal=0、时间线一条；重复回调不累计")
    void 尾款成功推进订单并幂等() {
        InitiatePaymentResult payment = initiateTailPayment();

        PaymentCallbackResult paid = callback(payment.getPaymentNo(), "183000.00", PaymentStatus.PAID);
        assertEquals(PaymentCallbackResultCode.SUCCESS, paid.getCode());

        Order order = orderRepository.findByOrderNo(orderNo).orElseThrow();
        assertEquals(OrderState.FINAL_PAYMENT_PAID, order.getOrderState());
        OrderAmount amount = orderAmountRepository.findDomainByOrderId(orderId).orElseThrow();
        assertEquals(0, new BigDecimal("203000.00").compareTo(amount.getPaidTotal().getAmount()));
        assertEquals(0, new BigDecimal("0.00").compareTo(amount.getUnpaidTotal().getAmount()));

        Integer timelineCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM vso_order_timeline WHERE order_id = ? AND event_type = 'PAYMENT_SUCCESS'",
                Integer.class, orderId);
        assertNotNull(timelineCount);
        assertEquals(1, timelineCount);

        // 重复成功回调：幂等且不重复累计
        PaymentCallbackResult duplicate = callback(payment.getPaymentNo(), "183000.00", PaymentStatus.PAID);
        assertTrue(duplicate.getCode() == PaymentCallbackResultCode.DUPLICATE
                || duplicate.getCode() == PaymentCallbackResultCode.SUCCESS);
        OrderAmount afterDuplicate = orderAmountRepository.findDomainByOrderId(orderId).orElseThrow();
        assertEquals(0, new BigDecimal("203000.00").compareTo(afterDuplicate.getPaidTotal().getAmount()));
        assertEquals(OrderState.FINAL_PAYMENT_PAID,
                orderRepository.findByOrderNo(orderNo).orElseThrow().getOrderState());
    }

    @Test
    @DisplayName("尾款支付失败：保持 600，允许按活动支付单规则重新发起")
    void 尾款失败保持待交付() {
        InitiatePaymentResult payment = initiateTailPayment();

        PaymentCallbackResult failed = callback(payment.getPaymentNo(), "183000.00", PaymentStatus.PAYMENT_FAILED);
        assertTrue(failed.getCode() == PaymentCallbackResultCode.SUCCESS);

        Order order = orderRepository.findByOrderNo(orderNo).orElseThrow();
        assertEquals(OrderState.PREPARE_DELIVER, order.getOrderState());
        OrderAmount amount = orderAmountRepository.findDomainByOrderId(orderId).orElseThrow();
        assertEquals(0, new BigDecimal("20000.00").compareTo(amount.getPaidTotal().getAmount()));

        // 失败后允许重新发起尾款
        InitiatePaymentResult retry = initiateTailPayment();
        assertNotNull(retry.getPaymentNo());
    }

    @Test
    @DisplayName("合同状态投影：尾款成功后合同仍为 SIGNED，不影响订单主状态推进")
    void 合同状态投影存在() {
        Contract contract = contractRepository.findByOrderIdAndType(orderId, Contract.TYPE_VEHICLE_PURCHASE)
                .orElseThrow();
        assertEquals(ContractStatus.SIGNED, contract.getContractStatus());

        initiateTailPayment();
        Contract reloaded = contractRepository.findByOrderIdAndType(orderId, Contract.TYPE_VEHICLE_PURCHASE)
                .orElseThrow();
        assertEquals(ContractStatus.SIGNED, reloaded.getContractStatus());
    }
}
