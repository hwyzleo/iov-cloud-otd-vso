package net.hwyz.iov.cloud.otd.vso.service.application.service;

import net.hwyz.iov.cloud.otd.vso.api.enums.ContractStatus;
import net.hwyz.iov.cloud.otd.vso.api.enums.OrderType;
import net.hwyz.iov.cloud.otd.vso.api.enums.PaymentChannel;
import net.hwyz.iov.cloud.otd.vso.api.enums.PaymentStage;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.ContractCallbackCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.InitiateContractSignCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.InitiatePaymentCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.enums.PaymentCallbackResultCode;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.ContractSignResult;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.InitiatePaymentResult;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.PaymentCallbackResult;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.OrderNotExistException;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Contract;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Order;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderAmount;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderState;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.shared.Money;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.ContractRepository;
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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 购车合同签署链路集成测试（OTD-VSO-DSN-CR-014 US-032）
 * <p>
 * 覆盖：发起签署（DRAFT→GENERATED→SIGNING）→ 生产验签回调 SIGNED → 状态投影/时间线/回调日志；
 * 重复回调幂等；Mock 自回调默认关闭；双账号隔离。
 * 本测试为本地数据库事务内集成测试，不代表真实跨服务 E2E 或生产验收。
 *
 * @author VSO Team
 */
@SpringBootTest(properties = {
        "spring.main.allow-bean-definition-overriding=true",
        "iov.kafka.topic-provisioning.enabled=false"
})
@Transactional
@DisplayName("购车合同签署 - 集成测试")
class ContractSignIntegrationTest {

    @Autowired
    private ContractAppService contractAppService;
    @Autowired
    private ContractCallbackService contractCallbackService;
    @Autowired
    private ContractRepository contractRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private PaymentAppService paymentAppService;
    @Autowired
    private CallbackSignatureService callbackSignatureService;
    @Autowired
    private MockContractAutoCallbackService mockContractAutoCallbackService;
    @Autowired
    private MockPaymentAutoCallbackService mockPaymentAutoCallbackService;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private String account;
    private String orderNo;
    private String orderId;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(mockContractAutoCallbackService, "autoCallbackEnabled", false);
        ReflectionTestUtils.setField(mockPaymentAutoCallbackService, "autoCallbackSetting", "false");
        account = "contract_user_" + System.nanoTime();
        createOrder(account, "full_payment", OrderState.PREPARE_DELIVER);
    }

    private void createOrder(String owner, String paymentMethod, OrderState state) {
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
                .orderState(state)
                .paymentMethod(paymentMethod)
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
                        + "authorized_flag, row_valid) VALUES (?, ?, 'order_user', ?, '合同测试用户', 0, 1)",
                "PID" + suffix, orderId, owner);
    }

    private ContractSignResult initiate(String owner) {
        return contractAppService.initiateSign(InitiateContractSignCmd.builder()
                .accountId(owner)
                .orderNo(orderNo)
                .build());
    }

    private PaymentCallbackResult signedCallback(ContractSignResult signing) {
        Instant occurredAt = Instant.now();
        long ts = Instant.now().getEpochSecond();
        String nonce = UUID.randomUUID().toString();
        Map<String, String> signData = new LinkedHashMap<>();
        signData.put("contractNo", signing.getContractNo());
        signData.put("signNo", signing.getSignNo());
        signData.put("status", ContractStatus.SIGNED.name());
        signData.put("occurredAt", occurredAt.toString());
        String signature = callbackSignatureService.sign(signData, nonce, ts);
        return contractCallbackService.handleCallback(ContractCallbackCmd.builder()
                .contractNo(signing.getContractNo())
                .signNo(signing.getSignNo())
                .status(ContractStatus.SIGNED)
                .occurredAt(occurredAt)
                .idempotentKey("IDEM-" + UUID.randomUUID())
                .build(), signature, ts, nonce);
    }

    private int count(String sql, Object... args) {
        Integer value = jdbcTemplate.queryForObject(sql, Integer.class, args);
        return value == null ? 0 : value;
    }

    @Test
    @DisplayName("发起签署：SIGNING 落库，签名回调后 SIGNED、投影与时间线一致，随后可发起尾款")
    void 发起签署并回调成功() {
        ContractSignResult signing = initiate(account);

        assertEquals(ContractStatus.SIGNING.name(), signing.getContractStatus());
        assertNotNull(signing.getSignNo());
        assertNotNull(signing.getSignUrl());
        Contract persisted = contractRepository.findByOrderIdAndType(orderId, Contract.TYPE_VEHICLE_PURCHASE)
                .orElseThrow();
        assertEquals(signing.getSignNo(), persisted.getSignNo());
        assertEquals(1, count("SELECT COUNT(*) FROM vso_order_timeline WHERE order_id = ? "
                + "AND event_type = 'CONTRACT_GENERATED'", orderId));
        assertEquals(1, count("SELECT COUNT(*) FROM vso_order_timeline WHERE order_id = ? "
                + "AND event_type = 'CONTRACT_SIGNING'", orderId));

        PaymentCallbackResult result = signedCallback(signing);
        assertEquals(PaymentCallbackResultCode.SUCCESS, result.getCode());

        Contract signed = contractRepository.findByOrderIdAndType(orderId, Contract.TYPE_VEHICLE_PURCHASE)
                .orElseThrow();
        assertEquals(ContractStatus.SIGNED, signed.getContractStatus());
        assertNotNull(signed.getSignTime());
        assertEquals(1, count("SELECT COUNT(*) FROM vso_order_status_dimension WHERE order_id = ? "
                + "AND contract_status = 'SIGNED'", orderId));
        assertEquals(1, count("SELECT COUNT(*) FROM vso_order_timeline WHERE order_id = ? "
                + "AND event_type = 'CONTRACT_SIGNED'", orderId));
        assertEquals(1, count("SELECT COUNT(*) FROM vso_callback_log WHERE order_id = ? "
                + "AND business_type = 'CONTRACT' AND process_result = 'SUCCESS'", orderId));

        // 合同签署后：复用既有支付入口发起尾款
        InitiatePaymentResult tail = paymentAppService.initiatePayment(InitiatePaymentCmd.builder()
                .accountId(account).orderNo(orderNo).paymentChannel(PaymentChannel.WECHAT).build());
        assertEquals(PaymentStage.TAIL_PAYMENT.name(), tail.getPaymentStage());
        assertEquals(0, new BigDecimal("183000.00").compareTo(tail.getPaymentAmount()));
        assertEquals(OrderState.PREPARE_DELIVER,
                orderRepository.findByOrderNo(orderNo).orElseThrow().getOrderState(),
                "合同签署不改变订单主状态");
    }

    @Test
    @DisplayName("重复 SIGNED 回调：只一条签署成功时间线，不覆盖已生效事实")
    void 重复签署回调幂等() {
        ContractSignResult signing = initiate(account);
        assertEquals(PaymentCallbackResultCode.SUCCESS, signedCallback(signing).getCode());
        PaymentCallbackResult duplicate = signedCallback(signing);

        assertEquals(PaymentCallbackResultCode.DUPLICATE, duplicate.getCode());
        assertEquals(1, count("SELECT COUNT(*) FROM vso_order_timeline WHERE order_id = ? "
                + "AND event_type = 'CONTRACT_SIGNED'", orderId));
    }

    @Test
    @DisplayName("Mock 自回调开启：服务端签名回调使合同直接 SIGNED")
    void Mock自回调开启() {
        ReflectionTestUtils.setField(mockContractAutoCallbackService, "autoCallbackEnabled", true);

        ContractSignResult signing = initiate(account);

        Contract contract = contractRepository.findByOrderIdAndType(orderId, Contract.TYPE_VEHICLE_PURCHASE)
                .orElseThrow();
        assertEquals(ContractStatus.SIGNED, contract.getContractStatus());
        assertEquals(signing.getSignNo(), contract.getSignNo());
        assertEquals(1, count("SELECT COUNT(*) FROM vso_order_timeline WHERE order_id = ? "
                + "AND event_type = 'CONTRACT_SIGNED'", orderId));
    }

    @Test
    @DisplayName("双账号隔离：他人订单按 ORDER_NOT_EXIST 处理，不泄露合同事实")
    void 双账号隔离() {
        initiate(account);

        assertThrows(OrderNotExistException.class,
                () -> contractAppService.initiateSign(InitiateContractSignCmd.builder()
                        .accountId("other_" + System.nanoTime()).orderNo(orderNo).build()));
        assertThrows(OrderNotExistException.class,
                () -> contractAppService.getContract("other_" + System.nanoTime(), orderNo));
    }

    @Test
    @DisplayName("非全款订单拒绝发起合同签署")
    void 非全款拒绝() {
        account = "loan_user_" + System.nanoTime();
        createOrder(account, "loan", OrderState.PREPARE_DELIVER);

        assertThrows(net.hwyz.iov.cloud.otd.vso.service.common.exception.TailPaymentNotAllowedException.class,
                () -> initiate(account));
    }
}
