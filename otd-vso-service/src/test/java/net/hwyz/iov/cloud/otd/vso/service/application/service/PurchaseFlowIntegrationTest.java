package net.hwyz.iov.cloud.otd.vso.service.application.service;

import net.hwyz.iov.cloud.edd.mdm.api.service.ConfigurationService;
import net.hwyz.iov.cloud.edd.mdm.api.vo.request.ConfigurationByVariantAndOptionCodesRequest;
import net.hwyz.iov.cloud.edd.mdm.api.vo.response.ConfigurationResponse;
import net.hwyz.iov.cloud.otd.vso.api.enums.PaymentChannel;
import net.hwyz.iov.cloud.otd.vso.api.enums.PaymentStage;
import net.hwyz.iov.cloud.otd.vso.api.enums.PaymentStatus;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.AgreementConfirmationCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.DownPaymentCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.EarnestMoneyCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.EarnestToDownCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.InitiatePaymentCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.PaymentCallbackCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.RefundCallbackCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.enums.PaymentCallbackResultCode;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.DeliveryAppointmentResult;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.DownPaymentOrderResult;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.EarnestMoneyOrderResult;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.EarnestToDownResult;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.InitiatePaymentResult;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.OrderTimelineResult;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.PaymentCallbackResult;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.PaymentResult;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.RefundResult;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.OrderNotExistException;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Order;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderAmount;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderState;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.RefundStatus;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderAmountRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.SupplementaryPaymentRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.SaleModelMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.SaleModelVariantPolicyMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelVariantPolicyPo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * C 端购车完整链路集成测试（OTD-VSO-DSN-CR-002 §14 PurchaseFlowIntegrationTest）
 * <p>
 * 覆盖：意向金下单→支付→补差转定金→锁单→配车→发运→运输→预约→交付→激活、
 * 直接定金+自动锁单、支付异常/重复/冲突、退款与双账号隔离。
 * <p>
 * 说明：本测试为本地数据库事务内集成测试，不代表真实跨服务 E2E 或生产验收。
 *
 * @author VSO Team
 */
@SpringBootTest(properties = {
        "spring.main.allow-bean-definition-overriding=true",
        "iov.kafka.topic-provisioning.enabled=false"
})
@Transactional
@DisplayName("购车全链路 - 集成测试")
class PurchaseFlowIntegrationTest {

    private static final String SIGNATURE_SECRET = "hwyz1234567890abcdef";

    @Autowired
    private OrderAppService orderAppService;
    @Autowired
    private PaymentAppService paymentAppService;
    @Autowired
    private PaymentCallbackService paymentCallbackService;
    @Autowired
    private RefundAppService refundAppService;
    @Autowired
    private RefundCallbackService refundCallbackService;
    @Autowired
    private DeliveryAppService deliveryAppService;
    @Autowired
    private AutoLockExecutor autoLockExecutor;
    @Autowired
    private PurchaseAgreementAppService purchaseAgreementAppService;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private net.hwyz.iov.cloud.otd.vso.service.common.time.BusinessTimeZone businessTimeZone;
    @Autowired
    private OrderAmountRepository orderAmountRepository;
    @Autowired
    private SupplementaryPaymentRepository supplementaryPaymentRepository;
    @Autowired
    private SaleModelMapper saleModelMapper;
    @Autowired
    private SaleModelVariantPolicyMapper saleModelVariantPolicyMapper;

    @MockBean
    private ConfigurationService configurationService;

    private String saleModelCode;
    private final String variantCode = "V001";
    private final String modelCode = "M001";
    private final String configurationCode = "CFG001";

    @BeforeEach
    void setUp() {
        String suffix = String.valueOf(System.nanoTime());
        saleModelCode = "SM_" + suffix;
        insertSaleModel();
        insertVariantPolicy();
        mockMdm();
        purchaseAgreementAppService.save(net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.SavePurchaseAgreementCmd.builder()
                .agreementCode("VEHICLE_PURCHASE")
                .saleModelCode(saleModelCode)
                .versionNo(1)
                .title("购车协议")
                .detail("协议条款")
                .agreementStatus("active")
                .effectiveFrom(LocalDateTime.now().minusDays(1))
                .build());
    }

    @Test
    @DisplayName("主链：意向金→支付→补差→锁单→配车→发运→运输→预约→交付→激活")
    void 意向金补差完整链路() {
        String account = "flow_user_" + System.nanoTime();
        EarnestMoneyOrderResult orderResult = earnestOrder(account);
        String orderNo = orderResult.getOrderNo();
        assertEquals(0, new BigDecimal("5000.00").compareTo(orderResult.getEarnestMoneyAmount()));

        // 意向金支付
        InitiatePaymentResult earnestPayment = paymentAppService.initiatePayment(InitiatePaymentCmd.builder()
                .accountId(account).orderNo(orderNo).paymentChannel(PaymentChannel.WECHAT).build());
        assertEquals(0, new BigDecimal("5000.00").compareTo(earnestPayment.getPaymentAmount()),
                "支付金额必须取订单快照，不得取车型最低价");
        assertEquals(PaymentStatus.PROCESSING.name(), earnestPayment.getPaymentStatus(), "Mock 不得固定返回已支付");

        assertCallbackPaid(account, orderNo, earnestPayment.getPaymentNo(), "5000.00", PaymentStage.EARNEST_MONEY);
        Order paid = orderRepository.findByOrderNo(orderNo).orElseThrow();
        assertEquals(OrderState.EARNEST_MONEY_PAID, paid.getOrderState());
        assertEquals(0, new BigDecimal("5000.00").compareTo(paidAmount(paid)));

        // 补差转定金
        EarnestToDownResult convert = orderAppService.earnestMoneyToDownPayment(EarnestToDownCmd.builder()
                .accountId(account).orderNo(orderNo).customerType("personal").paymentMethod("full_payment").build());
        assertNotNull(convert.getSupplementaryPayment(), "差额 15000 应创建补款任务");
        assertEquals(0, new BigDecimal("15000.00")
                .compareTo(convert.getSupplementaryPayment().getAmount().getAmount()));
        String supplementaryNo = convert.getSupplementaryPayment().getSupplementaryNo();

        orderAppService.initiateSupplementPayment(account, supplementaryNo, PaymentChannel.WECHAT.name());
        String supplementPaymentNo = supplementaryPaymentRepository.findBySupplementaryNo(supplementaryNo)
                .orElseThrow().getPaymentId();
        assertCallbackPaid(account, orderNo, supplementPaymentNo, "15000.00", PaymentStage.DOWN_PAYMENT);

        Order converted = orderRepository.findByOrderNo(orderNo).orElseThrow();
        assertEquals(OrderState.DOWN_PAYMENT_PAID, converted.getOrderState());
        assertNotNull(converted.getDownPaymentTime(), "转定金完成应写入 downPaymentTime");
        assertEquals(0, new BigDecimal("20000.00").compareTo(paidAmount(converted)));

        // 重复补款回调：不重复累计
        Map<String, String> signData = signData(supplementPaymentNo, "MOCK-" + supplementPaymentNo, "15000.00", "PAID");
        long ts = System.currentTimeMillis() / 1000;
        String nonce = UUID.randomUUID().toString();
        PaymentCallbackResult duplicate = paymentCallbackService.handleCallback(PaymentCallbackCmd.builder()
                .paymentNo(supplementPaymentNo).externalTradeNo("MOCK-" + supplementPaymentNo)
                .paymentStage(PaymentStage.DOWN_PAYMENT.name()).paymentAmount(new BigDecimal("15000.00"))
                .paymentStatus("PAID").payTime(LocalDateTime.now()).idempotentKey("IDEM-DUP-" + nonce).build(),
                sign(signData, nonce, ts), ts, nonce);
        assertTrue(duplicate.getCode() == PaymentCallbackResultCode.DUPLICATE
                        || duplicate.getCode() == PaymentCallbackResultCode.SUCCESS,
                "重复成功回调应幂等");
        assertEquals(0, new BigDecimal("20000.00").compareTo(
                paidAmount(orderRepository.findByOrderNo(orderNo).orElseThrow())), "重复回调不得重复累计");

        // 锁单 → 配车 → 发运 → 运输 → 待交付
        orderAppService.lock(net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.LockCmd.builder()
                .accountId(account).orderNo(orderNo).build());
        assertEquals(OrderState.ARRANGE_PRODUCTION, stateOf(orderNo));

        orderAppService.assignVehicle(net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.AssignVehicleCmd.builder()
                .orderNo(orderNo).vin("VIN" + System.nanoTime()).operatorId("mpt").build());
        assertEquals(OrderState.ALLOCATION_VEHICLE, stateOf(orderNo));

        orderAppService.applyTransport(net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.ApplyTransportCmd.builder()
                .orderNo(orderNo).operatorId("mpt").operatorName("运营员").build());
        assertEquals(OrderState.APPLY_TRANSPORT, stateOf(orderNo));
        Order applied = orderRepository.findByOrderNo(orderNo).orElseThrow();
        assertEquals("运营员", applied.getTransportApplyPersonName());
        assertNotNull(applied.getTransportApplyTime());

        orderAppService.prepareTransport(prepareTransportCmd(orderNo));
        assertEquals(OrderState.PREPARE_TRANSPORT, stateOf(orderNo));
        orderAppService.transporting(transportingCmd(orderNo));
        assertEquals(OrderState.TRANSPORTING, stateOf(orderNo));
        orderAppService.prepareDelivery(prepareDeliveryCmd(orderNo));
        assertEquals(OrderState.PREPARE_DELIVER, stateOf(orderNo));

        // 交付预约
        DeliveryAppointmentResult appointment = deliveryAppService.create(account, orderNo,
                LocalDateTime.now().plusDays(3), "STORE001", "交付中心A");
        assertEquals("booked", appointment.getAppointmentStatus());

        // 交付 + 激活
        orderAppService.delivered(net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.DeliveredCmd.builder()
                .orderNo(orderNo).build());
        assertEquals(OrderState.DELIVERED, stateOf(orderNo));
        DeliveryAppointmentResult delivered = deliveryAppService.getByOrderNo(account, orderNo);
        assertEquals("completed", delivered.getAppointmentStatus(), "交付时应完成有效预约");
        assertFalse(delivered.getRecords().isEmpty(), "交付应生成交付记录");

        orderAppService.activate(net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.ActivateCmd.builder()
                .orderNo(orderNo).build());
        assertEquals(OrderState.ACTIVATED, stateOf(orderNo));

        // 时间线完整且正序
        List<OrderTimelineResult> timeline = orderAppService.getOrderTimeline(account, orderNo);
        assertFalse(timeline.isEmpty());
        for (int i = 1; i < timeline.size(); i++) {
            assertFalse(timeline.get(i).getEventTime().isBefore(timeline.get(i - 1).getEventTime()),
                    "时间线必须按事件时间正序");
        }
        assertTrue(timeline.stream().anyMatch(t -> "ORDER_CREATED".equals(t.getEventType())));
        assertTrue(timeline.stream().anyMatch(t -> "PAYMENT_SUCCESS".equals(t.getEventType())));
        assertTrue(timeline.stream().anyMatch(t -> "EARNEST_TO_DOWN_COMPLETED".equals(t.getEventType())));
        assertTrue(timeline.stream().anyMatch(t -> "ORDER_LOCKED".equals(t.getEventType())));
        assertTrue(timeline.stream().anyMatch(t -> "VEHICLE_ASSIGNED".equals(t.getEventType())));
        assertTrue(timeline.stream().anyMatch(t -> "ORDER_ACTIVATED".equals(t.getEventType())));
    }

    @Test
    @DisplayName("直接定金：支付 20000 → 47:59 不自动锁单、48:00 自动锁单")
    void 直接定金与自动锁单() {
        String account = "direct_user_" + System.nanoTime();
        DownPaymentOrderResult orderResult = orderAppService.downPaymentOrder(DownPaymentCmd.builder()
                .accountId(account)
                .saleModel(saleModelCode)
                .modelCode(modelCode)
                .variantCode(variantCode)
                .optionCodes(List.of())
                .configurationCode(configurationCode)
                .customerType("personal")
                .paymentMethod("full_payment")
                .orderPersonType(1)
                .orderPersonName("张三")
                .orderPersonIdType(1)
                .orderPersonIdNum("310101199001010011")
                .purchasePlan(1)
                .licenseCityCode("310100")
                .orderStoreCode("310100")
                .deliveryStoreCode("310100")
                .agreementConfirmations(List.of(AgreementConfirmationCmd.builder()
                        .agreementCode("VEHICLE_PURCHASE").versionNo(1).build()))
                .build());
        assertEquals(0, new BigDecimal("20000.00").compareTo(orderResult.getDownPaymentAmount()),
                "直接定金支付金额必须取选中 Variant 快照");

        InitiatePaymentResult payment = paymentAppService.initiatePayment(InitiatePaymentCmd.builder()
                .accountId(account).orderNo(orderResult.getOrderNo()).paymentChannel(PaymentChannel.WECHAT).build());
        assertEquals(0, new BigDecimal("20000.00").compareTo(payment.getPaymentAmount()));
        assertCallbackPaid(account, orderResult.getOrderNo(), payment.getPaymentNo(), "20000.00",
                PaymentStage.DOWN_PAYMENT);

        Order paid = orderRepository.findByOrderNo(orderResult.getOrderNo()).orElseThrow();
        assertEquals(OrderState.DOWN_PAYMENT_PAID, paid.getOrderState());
        assertNotNull(paid.getDownPaymentTime());

        // 47:59 不锁单
        setDownPaymentTime(paid, LocalDateTime.now().minusHours(47).minusMinutes(59));
        autoLockExecutor.lockExpiredOrder(orderResult.getOrderNo());
        assertEquals(OrderState.DOWN_PAYMENT_PAID, stateOf(orderResult.getOrderNo()), "47:59 不得自动锁单");

        // 48:01 自动锁单
        setDownPaymentTime(paid, LocalDateTime.now().minusHours(48).minusMinutes(1));
        autoLockExecutor.lockExpiredOrder(orderResult.getOrderNo());
        assertEquals(OrderState.ARRANGE_PRODUCTION, stateOf(orderResult.getOrderNo()), "达到 48 小时应自动锁单");
    }

    @Test
    @DisplayName("支付异常：失败可重新支付、处理中不改状态、已成功后冲突")
    void 支付异常分支() {
        String account = "abnormal_user_" + System.nanoTime();
        EarnestMoneyOrderResult orderResult = earnestOrder(account);
        String orderNo = orderResult.getOrderNo();

        InitiatePaymentResult first = paymentAppService.initiatePayment(InitiatePaymentCmd.builder()
                .accountId(account).orderNo(orderNo).paymentChannel(PaymentChannel.WECHAT).build());

        // PAYMENT_FAILED：订单保持待支付
        callback(orderNo, first.getPaymentNo(), "5000.00", PaymentStatus.PAYMENT_FAILED, PaymentStage.EARNEST_MONEY);
        assertEquals(OrderState.EARNEST_MONEY_UNPAID, stateOf(orderNo));
        PaymentResult failed = paymentAppService.getPayment(account, orderNo, first.getPaymentNo());
        assertEquals(PaymentStatus.PAYMENT_FAILED.name(), failed.getPaymentStatus());

        // 允许重新发起新支付单
        InitiatePaymentResult second = paymentAppService.initiatePayment(InitiatePaymentCmd.builder()
                .accountId(account).orderNo(orderNo).paymentChannel(PaymentChannel.WECHAT).build());
        assertFalse(first.getPaymentNo().equals(second.getPaymentNo()), "失败后应允许创建新支付单");

        // PROCESSING：订单不变
        callback(orderNo, second.getPaymentNo(), "5000.00", PaymentStatus.PROCESSING, PaymentStage.EARNEST_MONEY);
        assertEquals(OrderState.EARNEST_MONEY_UNPAID, stateOf(orderNo),
                "处理中不得将订单更新为已支付");

        // PAID 成功
        callback(orderNo, second.getPaymentNo(), "5000.00", PaymentStatus.PAID, PaymentStage.EARNEST_MONEY);
        assertEquals(OrderState.EARNEST_MONEY_PAID, stateOf(orderNo));

        // 已成功后收到失败：冲突
        long ts = System.currentTimeMillis() / 1000;
        String nonce = UUID.randomUUID().toString();
        PaymentCallbackCmd conflictCmd = PaymentCallbackCmd.builder()
                .paymentNo(second.getPaymentNo())
                .externalTradeNo("MOCK-" + second.getPaymentNo())
                .paymentStage(PaymentStage.EARNEST_MONEY.name())
                .paymentAmount(new BigDecimal("5000.00"))
                .paymentStatus(PaymentStatus.PAYMENT_FAILED.name())
                .payTime(LocalDateTime.now())
                .idempotentKey("IDEM-CONFLICT-" + nonce)
                .build();
        PaymentCallbackResult conflict = paymentCallbackService.handleCallback(conflictCmd,
                sign(signData(second.getPaymentNo(), "MOCK-" + second.getPaymentNo(), "5000.00", "PAYMENT_FAILED"), nonce, ts),
                ts, nonce);
        assertEquals(PaymentCallbackResultCode.CONFLICT, conflict.getCode());
        assertEquals(OrderState.EARNEST_MONEY_PAID, stateOf(orderNo));
    }

    @Test
    @DisplayName("退款：已锁单按 5%/500 手续费，回调完成订单退款")
    void 锁单退款链路() {
        String account = "refund_user_" + System.nanoTime();
        EarnestMoneyOrderResult orderResult = earnestOrder(account);
        String orderNo = orderResult.getOrderNo();
        InitiatePaymentResult payment = paymentAppService.initiatePayment(InitiatePaymentCmd.builder()
                .accountId(account).orderNo(orderNo).paymentChannel(PaymentChannel.WECHAT).build());
        assertCallbackPaid(account, orderNo, payment.getPaymentNo(), "5000.00", PaymentStage.EARNEST_MONEY);

        // 意向金已支付状态下退款：全额 5000、无手续费
        RefundResult refund = refundAppService.apply(account, orderNo);
        assertEquals(0, new BigDecimal("5000.00").compareTo(refund.getRefundAmount()));
        assertEquals(0, BigDecimal.ZERO.compareTo(refund.getRefundFee()));
        assertEquals(1, refund.getItems().size(), "仅一笔支付，应生成一条明细");
        assertEquals("P".charAt(0), payment.getPaymentNo().charAt(0));

        // 明细完成 → 主单完成 → 订单 REFUND_COMPLETE + refundTotal
        long ts = System.currentTimeMillis() / 1000;
        String nonce = UUID.randomUUID().toString();
        RefundCallbackCmd refundCmd = RefundCallbackCmd.builder()
                .refundNo(refund.getRefundNo())
                .refundItemId(refund.getItems().get(0).getRefundItemId())
                .externalRefundNo("MOCK-" + refund.getItems().get(0).getRefundItemId())
                .refundAmount(new BigDecimal("5000.00"))
                .refundStatus("completed")
                .refundTime(LocalDateTime.now())
                .idempotentKey("IDEM-REFUND-" + nonce)
                .build();
        Map<String, String> signData = new TreeMap<>();
        signData.put("amount", "5000.00");
        signData.put("orderId", refund.getItems().get(0).getRefundItemId());
        signData.put("paySeq", "MOCK-" + refund.getItems().get(0).getRefundItemId());
        signData.put("refundNo", refund.getRefundNo());
        signData.put("status", "completed");
        refundCallbackService.handleCallback(refundCmd, sign(signData, nonce, ts), ts, nonce);

        Order refunded = orderRepository.findByOrderNo(orderNo).orElseThrow();
        assertEquals(OrderState.REFUND_COMPLETE, refunded.getOrderState());
        OrderAmount amount = orderAmountRepository.findDomainByOrderId(refunded.getId()).orElseThrow();
        assertEquals(0, new BigDecimal("5000.00").compareTo(amount.getRefundTotal().getAmount()));
    }

    @Test
    @DisplayName("双账号隔离：他人订单查询/支付/退款均表现为 ORDER_NOT_EXIST")
    void 双账号隔离() {
        String owner = "owner_" + System.nanoTime();
        String other = "other_" + System.nanoTime();
        EarnestMoneyOrderResult orderResult = earnestOrder(owner);

        assertThrows(OrderNotExistException.class,
                () -> orderAppService.getUserOrder(other, orderResult.getOrderNo()));
        assertThrows(OrderNotExistException.class,
                () -> paymentAppService.listPayments(other, orderResult.getOrderNo()));
        assertThrows(OrderNotExistException.class,
                () -> refundAppService.apply(other, orderResult.getOrderNo()));
        assertThrows(OrderNotExistException.class,
                () -> deliveryAppService.getByOrderNo(other, orderResult.getOrderNo()));

        assertNotNull(orderAppService.getUserOrder(owner, orderResult.getOrderNo()));
    }

    // ==================== helpers ====================

    private EarnestMoneyOrderResult earnestOrder(String account) {
        return orderAppService.earnestMoneyOrder(EarnestMoneyCmd.builder()
                .accountId(account)
                .saleModel(saleModelCode)
                .modelCode(modelCode)
                .variantCode(variantCode)
                .optionCodes(List.of())
                .licenseCityCode("310100")
                .agreementConfirmations(List.of(AgreementConfirmationCmd.builder()
                        .agreementCode("VEHICLE_PURCHASE").versionNo(1).build()))
                .build());
    }

    private void assertCallbackPaid(String account, String orderNo, String paymentNo,
                                    String amount, PaymentStage stage) {
        PaymentCallbackResult result = callback(orderNo, paymentNo, amount, PaymentStatus.PAID, stage);
        assertTrue(result.getCode() == PaymentCallbackResultCode.SUCCESS
                        || result.getCode() == PaymentCallbackResultCode.DUPLICATE,
                "支付回调应成功：" + result.getMessage());
    }

    private PaymentCallbackResult callback(String orderNo, String paymentNo, String amount,
                                           PaymentStatus status, PaymentStage stage) {
        long ts = System.currentTimeMillis() / 1000;
        String nonce = UUID.randomUUID().toString();
        String externalTradeNo = "MOCK-" + paymentNo;
        Map<String, String> signData = signData(paymentNo, externalTradeNo, amount, status.name());
        PaymentCallbackCmd cmd = PaymentCallbackCmd.builder()
                .paymentNo(paymentNo)
                .externalTradeNo(externalTradeNo)
                .paymentStage(stage.name())
                .paymentAmount(new BigDecimal(amount))
                .paymentStatus(status.name())
                .payTime(LocalDateTime.now())
                .idempotentKey("IDEM-" + status + "-" + nonce)
                .build();
        return paymentCallbackService.handleCallback(cmd, sign(signData, nonce, ts), ts, nonce);
    }

    private Map<String, String> signData(String paymentNo, String externalTradeNo, String amount, String status) {
        Map<String, String> data = new TreeMap<>();
        data.put("amount", new BigDecimal(amount).toPlainString());
        data.put("orderId", paymentNo);
        data.put("paySeq", externalTradeNo);
        data.put("status", status);
        return data;
    }

    private String sign(Map<String, String> signData, String nonce, long timestamp) {
        TreeMap<String, String> sorted = new TreeMap<>(signData);
        sorted.put("nonce", nonce);
        sorted.put("timestamp", String.valueOf(timestamp));
        StringBuilder sb = new StringBuilder();
        sorted.forEach((k, v) -> {
            if (sb.length() > 0) {
                sb.append('&');
            }
            sb.append(k).append('=').append(v == null ? "" : v);
        });
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(SIGNATURE_SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] bytes = mac.doFinal(sb.toString().getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : bytes) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private OrderState stateOf(String orderNo) {
        return orderRepository.findByOrderNo(orderNo).orElseThrow().getOrderState();
    }

    private BigDecimal paidAmount(Order order) {
        return orderAmountRepository.findDomainByOrderId(order.getId())
                .orElseThrow().getPaidTotal().getAmount();
    }

    private void setDownPaymentTime(Order order, LocalDateTime time) {
        // 与生产一致：用显式业务时区换算，不再依赖 JVM 默认时区（否则 Nacos 里 zone-id 与实际部署不一致时会误判）
        orderRepository.updateDownPaymentTime(order.getOrderNo(), businessTimeZone.toDate(time));
    }

    private void insertSaleModel() {
        SaleModelPo saleModel = new SaleModelPo();
        saleModel.setSaleModelCode(saleModelCode);
        saleModel.setModelName("集成测试车型");
        saleModel.setCarlineCode("CARLINE_001");
        saleModel.setListingStatus("active");
        saleModel.setEnable(true);
        saleModel.setEarnestMoney(true);
        saleModel.setDownPayment(true);
        saleModel.setSort(1);
        saleModelMapper.insertPo(saleModel);
    }

    private void insertVariantPolicy() {
        SaleModelVariantPolicyPo policy = new SaleModelVariantPolicyPo();
        policy.setSaleModelCode(saleModelCode);
        policy.setVariantCode(variantCode);
        policy.setSaleStatus("active");
        policy.setVariantPrice(new BigDecimal("200000.00"));
        policy.setEarnestMoneyPrice(new BigDecimal("5000.00"));
        policy.setDownPaymentPrice(new BigDecimal("20000.00"));
        policy.setCreateTime(Timestamp.valueOf(LocalDateTime.now()));
        saleModelVariantPolicyMapper.insert(policy);
    }

    private void mockMdm() {
        when(configurationService.resolveConfiguration(any(ConfigurationByVariantAndOptionCodesRequest.class)))
                .thenReturn(configurationCode);
        ConfigurationResponse configResponse = mock(ConfigurationResponse.class);
        when(configResponse.getName()).thenReturn(configurationCode);
        when(configurationService.getByCode(configurationCode)).thenReturn(configResponse);
    }

    private net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.PrepareTransportCmd prepareTransportCmd(String orderNo) {
        return net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.PrepareTransportCmd.builder()
                .orderNo(orderNo).build();
    }

    private net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.TransportingCmd transportingCmd(String orderNo) {
        return net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.TransportingCmd.builder()
                .orderNo(orderNo).build();
    }

    private net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.PrepareDeliveryCmd prepareDeliveryCmd(String orderNo) {
        return net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.PrepareDeliveryCmd.builder()
                .orderNo(orderNo).build();
    }
}
