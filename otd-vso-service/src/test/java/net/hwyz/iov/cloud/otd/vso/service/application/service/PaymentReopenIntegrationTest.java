package net.hwyz.iov.cloud.otd.vso.service.application.service;

import net.hwyz.iov.cloud.edd.mdm.api.service.ConfigurationService;
import net.hwyz.iov.cloud.edd.mdm.api.vo.request.ConfigurationByVariantAndOptionCodesRequest;
import net.hwyz.iov.cloud.edd.mdm.api.vo.response.ConfigurationResponse;
import net.hwyz.iov.cloud.otd.vso.api.enums.PaymentChannel;
import net.hwyz.iov.cloud.otd.vso.api.enums.PaymentStage;
import net.hwyz.iov.cloud.otd.vso.api.enums.PaymentStatus;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.AgreementConfirmationCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.EarnestMoneyCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.InitiatePaymentCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.PaymentCallbackCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.ReopenPaymentCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.SavePurchaseAgreementCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.enums.PaymentCallbackResultCode;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.EarnestMoneyOrderResult;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.InitiatePaymentResult;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.PaymentCallbackResult;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.ReopenPaymentResult;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.OrderReopenLimitExceededException;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Order;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderState;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderTimeline;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderTimelineEventType;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderAmountRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderTimelineRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.service.OrderDomainService;
import net.hwyz.iov.cloud.otd.vso.service.domain.service.OrderLockService;
import net.hwyz.iov.cloud.otd.vso.service.domain.service.TimeoutNotifyService;
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
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 支付超时恢复集成测试（OTD-VSO-DSN-CR-005）
 * <p>
 * 覆盖：小订单超时失效 → 重新发起支付 → 再次支付成功；
 * 以及恢复次数上限。
 * <p>
 * 说明：本地数据库事务内集成测试；Redis（订单锁/超时任务）以 Mock 隔离，
 * 不代表真实跨服务 E2E 或生产验收。
 *
 * @author VSO Team
 */
@SpringBootTest(properties = {
        "spring.main.allow-bean-definition-overriding=true",
        "iov.kafka.topic-provisioning.enabled=false"
})
@Transactional
@DisplayName("支付超时恢复 - 集成测试")
class PaymentReopenIntegrationTest {

    private static final String SIGNATURE_SECRET = "hwyz1234567890abcdef";
    private static final String TIMEOUT_TASK = "SMALL_ORDER_PAY_TIMEOUT";

    @Autowired
    private OrderAppService orderAppService;
    @Autowired
    private PaymentAppService paymentAppService;
    @Autowired
    private PaymentCallbackService paymentCallbackService;
    @Autowired
    private PurchaseAgreementAppService purchaseAgreementAppService;
    @Autowired
    private OrderDomainService orderDomainService;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderAmountRepository orderAmountRepository;
    @Autowired
    private OrderTimelineRepository orderTimelineRepository;
    @Autowired
    private MockPaymentAutoCallbackService mockPaymentAutoCallbackService;
    @Autowired
    private SaleModelMapper saleModelMapper;
    @Autowired
    private SaleModelVariantPolicyMapper saleModelVariantPolicyMapper;

    @MockBean
    private ConfigurationService configurationService;
    @MockBean
    private TimeoutNotifyService timeoutNotifyService;
    @MockBean
    private OrderLockService orderLockService;

    private String saleModelCode;
    private final String variantCode = "V001";
    private final String modelCode = "M001";

    @BeforeEach
    void setUp() {
        // 本用例断言「Mock 不得固定返回已支付」，需关闭服务端自回调（OTD-VSO-DSN-CR-007）
        org.springframework.test.util.ReflectionTestUtils.setField(
                mockPaymentAutoCallbackService, "autoCallbackSetting", "false");
        String suffix = String.valueOf(System.nanoTime());
        saleModelCode = "SM_CR005_" + suffix;
        insertSaleModel();
        insertVariantPolicy();
        mockMdm();
        purchaseAgreementAppService.save(SavePurchaseAgreementCmd.builder()
                .agreementCode("VEHICLE_PURCHASE")
                .saleModelCode(saleModelCode)
                .versionNo(1)
                .title("购车协议")
                .detail("协议条款")
                .agreementStatus("active")
                .effectiveFrom(LocalDateTime.now().minusDays(1))
                .build());

        lenient().when(orderLockService.executeWithLock(anyString(), anyString(), anyString(), any(Supplier.class)))
                .thenAnswer(invocation -> ((Supplier<?>) invocation.getArgument(3)).get());
    }

    @Test
    @DisplayName("超时失效 → 重新发起支付 → 支付成功：状态/金额/时间线/窗口重建")
    void 失效后恢复并支付成功() {
        String account = "reopen_user_" + System.nanoTime();
        String orderNo = createEarnestOrder(account);
        String orderId = orderRepository.findByOrderNo(orderNo).orElseThrow().getId();

        // 模拟 30 分钟超时失效
        expireOrder(orderNo);
        assertEquals(OrderState.EXPIRED, stateOf(orderNo));

        // 重新发起支付
        ReopenPaymentResult reopen = orderAppService.reopenForPayment(ReopenPaymentCmd.builder()
                .accountId(account).orderNo(orderNo).build());
        assertEquals(OrderState.EARNEST_MONEY_UNPAID.getValue(), reopen.getOrderState());
        assertEquals(1, reopen.getReopenCount());
        assertNotNull(reopen.getExpireTime());
        assertEquals(OrderState.EARNEST_MONEY_UNPAID, stateOf(orderNo));
        verify(timeoutNotifyService).cancelByOrderIdAndType(orderId, TIMEOUT_TASK);
        // 下单时 1 次 + 恢复时重建 1 次
        verify(timeoutNotifyService, times(2))
                .createTimeoutTask(eq(orderId), eq(TIMEOUT_TASK), eq("invalid"), anyInt());

        List<OrderTimeline> timelines = orderTimelineRepository.findByOrderId(orderId);
        assertTrue(timelines.stream().anyMatch(t -> OrderTimelineEventType.ORDER_REOPEN.getCode()
                        .equals(t.getEventType())
                        && String.valueOf(OrderState.EXPIRED.getValue()).equals(t.getBeforeStatus())
                        && String.valueOf(OrderState.EARNEST_MONEY_UNPAID.getValue()).equals(t.getAfterStatus())),
                "恢复必须写 ORDER_REOPEN 时间线（960 → 200）");
        assertTrue(timelines.stream().anyMatch(t -> "ORDER_INVALIDATE".equals(t.getEventType())),
                "超时失效应写 ORDER_INVALIDATE 时间线");

        // 恢复后支付成功
        InitiatePaymentResult payment = paymentAppService.initiatePayment(InitiatePaymentCmd.builder()
                .accountId(account).orderNo(orderNo).paymentChannel(PaymentChannel.WECHAT).build());
        assertEquals(PaymentStatus.PROCESSING.name(), payment.getPaymentStatus(), "Mock 不得固定返回已支付");
        assertEquals(0, new BigDecimal("5000.00").compareTo(payment.getPaymentAmount()));

        PaymentCallbackResult callback = paidCallback(payment.getPaymentNo());
        assertTrue(callback.getCode() == PaymentCallbackResultCode.SUCCESS
                || callback.getCode() == PaymentCallbackResultCode.DUPLICATE);

        Order paid = orderRepository.findByOrderNo(orderNo).orElseThrow();
        assertEquals(OrderState.EARNEST_MONEY_PAID, paid.getOrderState());
        assertEquals(0, new BigDecimal("5000.00").compareTo(
                orderAmountRepository.findDomainByOrderId(orderId).orElseThrow().getPaidTotal().getAmount()));

        // 再次恢复被拒（已支付）
        assertThrows(net.hwyz.iov.cloud.otd.vso.service.common.exception.OrderReopenNotAllowedException.class,
                () -> orderAppService.reopenForPayment(ReopenPaymentCmd.builder()
                        .accountId(account).orderNo(orderNo).build()));
    }

    @Test
    @DisplayName("恢复次数上限：第 3 次恢复返回 ORDER_REOPEN_LIMIT_EXCEEDED")
    void 恢复次数上限() {
        String account = "reopen_limit_" + System.nanoTime();
        String orderNo = createEarnestOrder(account);

        expireOrder(orderNo);
        assertEquals(1, orderAppService.reopenForPayment(cmd(account, orderNo)).getReopenCount());

        expireOrder(orderNo);
        assertEquals(2, orderAppService.reopenForPayment(cmd(account, orderNo)).getReopenCount());

        expireOrder(orderNo);
        assertThrows(OrderReopenLimitExceededException.class,
                () -> orderAppService.reopenForPayment(cmd(account, orderNo)));
        assertEquals(OrderState.EXPIRED, stateOf(orderNo));
    }

    // ==================== helpers ====================

    private ReopenPaymentCmd cmd(String account, String orderNo) {
        return ReopenPaymentCmd.builder().accountId(account).orderNo(orderNo).build();
    }

    /**
     * 走真实超时失效路径（含 ORDER_INVALIDATE 时间线与状态守卫）
     */
    private void expireOrder(String orderNo) {
        Order order = orderRepository.findByOrderNo(orderNo).orElseThrow();
        assertTrue(orderDomainService.invalidateSmallOrder(order.getId()), "待支付订单应可失效");
    }

    private String createEarnestOrder(String account) {
        EarnestMoneyOrderResult result = orderAppService.earnestMoneyOrder(EarnestMoneyCmd.builder()
                .accountId(account)
                .saleModel(saleModelCode)
                .modelCode(modelCode)
                .variantCode(variantCode)
                .optionCodes(List.of())
                .licenseCityCode("310100")
                .agreementConfirmations(List.of(AgreementConfirmationCmd.builder()
                        .agreementCode("VEHICLE_PURCHASE").versionNo(1).build()))
                .build());
        return result.getOrderNo();
    }

    private PaymentCallbackResult paidCallback(String paymentNo) {
        long ts = System.currentTimeMillis() / 1000;
        String nonce = UUID.randomUUID().toString();
        String externalTradeNo = "MOCK-" + paymentNo;
        Map<String, String> signData = new TreeMap<>();
        signData.put("amount", new BigDecimal("5000.00").toPlainString());
        signData.put("orderId", paymentNo);
        signData.put("paySeq", externalTradeNo);
        signData.put("status", PaymentStatus.PAID.name());
        PaymentCallbackCmd cmd = PaymentCallbackCmd.builder()
                .paymentNo(paymentNo)
                .externalTradeNo(externalTradeNo)
                .paymentStage(PaymentStage.EARNEST_MONEY.name())
                .paymentAmount(new BigDecimal("5000.00"))
                .paymentStatus(PaymentStatus.PAID.name())
                .payTime(LocalDateTime.now())
                .idempotentKey("IDEM-" + nonce)
                .build();
        return paymentCallbackService.handleCallback(cmd, sign(signData, nonce, ts), ts, nonce);
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
                .thenReturn("CFG001");
        ConfigurationResponse configResponse = mock(ConfigurationResponse.class);
        when(configResponse.getName()).thenReturn("CFG001");
        when(configurationService.getByCode("CFG001")).thenReturn(configResponse);
    }
}
