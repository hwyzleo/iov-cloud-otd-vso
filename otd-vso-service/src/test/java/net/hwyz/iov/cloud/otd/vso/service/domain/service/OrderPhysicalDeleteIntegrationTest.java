package net.hwyz.iov.cloud.otd.vso.service.domain.service;

import net.hwyz.iov.cloud.edd.mdm.api.service.ConfigurationService;
import net.hwyz.iov.cloud.edd.mdm.api.vo.request.ConfigurationByVariantAndOptionCodesRequest;
import net.hwyz.iov.cloud.edd.mdm.api.vo.response.ConfigurationResponse;
import net.hwyz.iov.cloud.otd.vso.api.enums.PaymentChannel;
import net.hwyz.iov.cloud.otd.vso.api.enums.PaymentStage;
import net.hwyz.iov.cloud.otd.vso.api.enums.PaymentStatus;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.AgreementConfirmationCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.EarnestMoneyCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.EarnestToDownCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.InitiatePaymentCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.PaymentCallbackCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.SavePurchaseAgreementCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.EarnestMoneyOrderResult;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.InitiatePaymentResult;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.PaymentCallbackResult;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.PhysicalDeleteResult;
import net.hwyz.iov.cloud.otd.vso.service.application.service.OrderAppService;
import net.hwyz.iov.cloud.otd.vso.service.application.service.PaymentAppService;
import net.hwyz.iov.cloud.otd.vso.service.application.service.PaymentCallbackService;
import net.hwyz.iov.cloud.otd.vso.service.application.service.PurchaseAgreementAppService;
import net.hwyz.iov.cloud.otd.vso.service.application.service.RefundAppService;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.OrderShadowDeleteMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.SaleModelMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.SaleModelVariantPolicyMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderShadowDeletePo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelVariantPolicyPo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 订单物理删除集成测试（OTD-VSO-DSN-CR-005）
 * <p>
 * 验证：物理删除后订单相关表全部清零，包括
 * 退款明细、补款任务、协议确认快照、回调日志、审计与操作日志、影子删除记录，
 * 以及 row_valid = 0 的历史逻辑删除行。
 * <p>
 * 说明：本地数据库事务内集成测试，回滚不提交；不代表生产迁移或验收。
 *
 * @author VSO Team
 */
@SpringBootTest(properties = {
        "spring.main.allow-bean-definition-overriding=true",
        "iov.kafka.topic-provisioning.enabled=false"
})
@Transactional
@DisplayName("物理删除 - 全量清理集成测试")
class OrderPhysicalDeleteIntegrationTest {

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
    private PurchaseAgreementAppService purchaseAgreementAppService;
    @Autowired
    private OrderPhysicalDeleteService orderPhysicalDeleteService;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private SaleModelMapper saleModelMapper;
    @Autowired
    private SaleModelVariantPolicyMapper saleModelVariantPolicyMapper;
    @Autowired
    private OrderShadowDeleteMapper orderShadowDeleteMapper;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockBean
    private ConfigurationService configurationService;
    @MockBean
    private TimeoutNotifyService timeoutNotifyService;

    private String saleModelCode;
    private final String variantCode = "V001";
    private final String modelCode = "M001";

    @BeforeEach
    void setUp() {
        saleModelCode = "SM_DEL_" + System.nanoTime();
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
    }

    @Test
    @DisplayName("物理删除后所有订单相关数据清零（含退款明细/补款/协议快照/日志/row_valid=0）")
    void 物理删除全量清理() {
        // ---------- 订单 A：支付 + 退款（产生 refund / refund_item） ----------
        String accountA = "del_a_" + System.nanoTime();
        String orderNoA = createEarnestOrder(accountA);
        String orderIdA = orderRepository.findByOrderNo(orderNoA).orElseThrow().getId();
        InitiatePaymentResult payA = paymentAppService.initiatePayment(InitiatePaymentCmd.builder()
                .accountId(accountA).orderNo(orderNoA).paymentChannel(PaymentChannel.WECHAT).build());
        assertTrue(paidCallback(payA.getPaymentNo()), "支付回调应成功");
        refundAppService.apply(accountA, orderNoA);

        // ---------- 订单 B：支付 + 转定金（产生 supplementary_payment） ----------
        String accountB = "del_b_" + System.nanoTime();
        String orderNoB = createEarnestOrder(accountB);
        String orderIdB = orderRepository.findByOrderNo(orderNoB).orElseThrow().getId();
        InitiatePaymentResult payB = paymentAppService.initiatePayment(InitiatePaymentCmd.builder()
                .accountId(accountB).orderNo(orderNoB).paymentChannel(PaymentChannel.WECHAT).build());
        assertTrue(paidCallback(payB.getPaymentNo()), "支付回调应成功");
        orderAppService.earnestMoneyToDownPayment(EarnestToDownCmd.builder()
                .accountId(accountB).orderNo(orderNoB)
                .customerType("personal").paymentMethod("full_payment").build());

        // ---------- 准备审计/日志数据，并制造一条 row_valid=0 的时间线 ----------
        for (String orderNo : List.of(orderNoA, orderNoB)) {
            jdbcTemplate.update("INSERT INTO vso_order_log "
                    + "(order_no, operate_client, account_id, operator, operate_type, operate_desc, operate_time) "
                    + "VALUES (?, 'capp', 'acc_test', 'op_test', 'TEST', '物理删除测试', now())", orderNo);
            OrderShadowDeletePo shadow = new OrderShadowDeletePo();
            shadow.setShadowDeleteId("SD" + UUID.randomUUID().toString().replace("-", ""));
            shadow.setOriginOrderNo(orderNo);
            shadow.setDeleteReason("测试");
            shadow.setBeforeOrderState(200);
            shadow.setComplianceDeleteFlag(0);
            shadow.setDeleteUserId("op_test");
            shadow.setDeleteTime(LocalDateTime.now());
            orderShadowDeleteMapper.insertPo(shadow);
        }
        jdbcTemplate.update("UPDATE vso_order_timeline SET row_valid = 0 WHERE order_id = ? LIMIT 1", orderIdA);

        // 前置断言：确有数据
        assertTrue(count("vso_refund", "order_id", orderIdA) > 0, "应存在退款主单");
        String refundIdA = jdbcTemplate.queryForObject(
                "SELECT refund_id FROM vso_refund WHERE order_id = ? LIMIT 1", String.class, orderIdA);
        assertTrue(count("vso_refund_item", "refund_id", refundIdA) > 0, "应存在退款明细");
        assertTrue(count("vso_supplementary_payment", "order_id", orderIdB) > 0, "应存在补款任务");
        assertTrue(count("vso_order_agreement_confirmation", "order_id", orderIdA) > 0, "应存在协议确认快照");
        assertTrue(count("vso_order_timeline", "order_id", orderIdA) > 0, "应存在时间线（含 row_valid=0 行）");

        // ---------- 物理删除 A ----------
        PhysicalDeleteResult resultA = orderPhysicalDeleteService
                .physicalDeleteOrder(orderIdA, "测试物理删除", "op_test", true);

        assertTrue(resultA.getTotalDeletedRecords() > 0);
        assertTrue(resultA.getTableDeleteCount().getOrDefault("vso_refund_item", 0) > 0,
                "退款明细必须被物理删除");
        assertEquals(0, count("vso_order", "order_id", orderIdA));
        assertEquals(0, count("vso_order_amount", "order_id", orderIdA));
        assertEquals(0, count("vso_order_party", "order_id", orderIdA));
        assertEquals(0, count("vso_order_timeline", "order_id", orderIdA), "时间线（含 row_valid=0 行）应全部删除");
        assertEquals(0, count("vso_order_vehicle_snapshot", "order_id", orderIdA));
        assertEquals(0, count("vso_payment", "order_id", orderIdA));
        assertEquals(0, count("vso_refund", "order_id", orderIdA));
        assertEquals(0, count("vso_refund_item", "refund_id", refundIdA), "退款明细必须被物理删除");
        assertEquals(0, count("vso_order_agreement_confirmation", "order_id", orderIdA));
        assertEquals(0, count("vso_callback_log", "order_id", orderIdA));
        assertEquals(0, count("vso_audit_log", "order_id", orderIdA));
        assertEquals(0, count("vso_order_log", "order_no", orderNoA), "操作日志应一并删除");
        assertEquals(0, count("vso_order_shadow_delete", "origin_order_no", orderNoA), "影子删除记录不保留");
        verify(timeoutNotifyService).cancelByOrderIdAndType(orderIdA, "SMALL_ORDER_PAY_TIMEOUT");

        // ---------- 物理删除 B ----------
        orderPhysicalDeleteService.physicalDeleteOrder(orderIdB, "测试物理删除", "op_test", true);

        assertEquals(0, count("vso_order", "order_id", orderIdB));
        assertEquals(0, count("vso_supplementary_payment", "order_id", orderIdB), "补款任务应一并删除");
        assertEquals(0, count("vso_order_agreement_confirmation", "order_id", orderIdB));
        assertEquals(0, count("vso_order_log", "order_no", orderNoB));
        assertEquals(0, count("vso_order_shadow_delete", "origin_order_no", orderNoB));

        // 订单 A 已被删除：再次删除应报 ORDER_NOT_EXIST
        assertEquals(0, count("vso_order", "order_id", orderIdA));
    }

    // ==================== helpers ====================

    private int count(String table, String column, String value) {
        Integer c = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM " + table + " WHERE " + column + " = ?", Integer.class, value);
        return c == null ? 0 : c;
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

    private boolean paidCallback(String paymentNo) {
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
        PaymentCallbackResult result = paymentCallbackService.handleCallback(cmd, sign(signData, nonce, ts), ts, nonce);
        return result.getCode() == net.hwyz.iov.cloud.otd.vso.service.application.dto.enums.PaymentCallbackResultCode.SUCCESS
                || result.getCode() == net.hwyz.iov.cloud.otd.vso.service.application.dto.enums.PaymentCallbackResultCode.DUPLICATE;
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
