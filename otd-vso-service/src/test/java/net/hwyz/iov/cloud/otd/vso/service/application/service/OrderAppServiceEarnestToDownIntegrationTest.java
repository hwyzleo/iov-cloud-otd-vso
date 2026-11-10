package net.hwyz.iov.cloud.otd.vso.service.application.service;

import net.hwyz.iov.cloud.edd.mdm.api.service.ConfigurationService;
import net.hwyz.iov.cloud.edd.mdm.api.vo.request.ConfigurationByVariantAndOptionCodesRequest;
import net.hwyz.iov.cloud.edd.mdm.api.vo.response.ConfigurationResponse;
import net.hwyz.iov.cloud.otd.vso.api.enums.OrderType;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.EarnestMoneyCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.EarnestToDownCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.EarnestMoneyOrderResult;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.EarnestToDownResult;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Order;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderState;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderAmountRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.SaleModelMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.SaleModelVariantPolicyMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderAmountPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelVariantPolicyPo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = {
        "spring.main.allow-bean-definition-overriding=true",
        "iov.kafka.topic-provisioning.enabled=false"
})
@Transactional
@DisplayName("意向金转定金 - 集成测试")
class OrderAppServiceEarnestToDownIntegrationTest {

    @Autowired
    private OrderAppService orderAppService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderAmountRepository orderAmountRepository;

    @Autowired
    private SaleModelMapper saleModelMapper;

    @Autowired
    private SaleModelVariantPolicyMapper saleModelVariantPolicyMapper;

    @MockBean
    private ConfigurationService configurationService;

    private static final String SALE_MODEL_CODE = "INTG_E2D_SM_" + System.currentTimeMillis();
    private static final String MODEL_CODE = "INTG_E2D_MODEL";
    private static final String VARIANT_CODE = "INTG_E2D_VAR";
    private static final String CONFIGURATION_CODE = "INTG_E2D_CONFIG";
    private static final String CARLINE_CODE = "CARLINE_001";

    private void insertSaleModel() {
        SaleModelPo saleModel = new SaleModelPo();
        saleModel.setSaleModelCode(SALE_MODEL_CODE);
        saleModel.setModelName("集成测试车型");
        saleModel.setCarlineCode(CARLINE_CODE);
        saleModel.setListingStatus("active");
        saleModel.setEnable(true);
        saleModel.setEarnestMoney(true);
        saleModel.setDownPayment(false);
        saleModel.setSort(1);
        saleModelMapper.insertPo(saleModel);
    }

    private void insertVariantPolicy() {
        SaleModelVariantPolicyPo policy = new SaleModelVariantPolicyPo();
        policy.setSaleModelCode(SALE_MODEL_CODE);
        policy.setVariantCode(VARIANT_CODE);
        policy.setSaleStatus("active");
        policy.setVariantPrice(BigDecimal.valueOf(200000));
        policy.setEarnestMoneyPrice(BigDecimal.valueOf(5000));
        policy.setDownPaymentPrice(BigDecimal.valueOf(20000));
        policy.setCreateTime(Timestamp.from(Instant.now()));
        saleModelVariantPolicyMapper.insert(policy);
    }

    private EarnestMoneyOrderResult createAndPayEarnestMoneyOrder(String accountId) {
        insertSaleModel();
        insertVariantPolicy();

        when(configurationService.resolveConfiguration(any(ConfigurationByVariantAndOptionCodesRequest.class)))
                .thenReturn(CONFIGURATION_CODE);
        ConfigurationResponse configResponse = mock(ConfigurationResponse.class);
        when(configResponse.getName()).thenReturn(CONFIGURATION_CODE);
        when(configurationService.getByCode(CONFIGURATION_CODE))
                .thenReturn(configResponse);

        EarnestMoneyCmd cmd = EarnestMoneyCmd.builder()
                .accountId(accountId)
                .saleModel(SALE_MODEL_CODE)
                .modelCode(MODEL_CODE)
                .variantCode(VARIANT_CODE)
                .optionCodes(null)
                .licenseCityCode("CITY_001")
                .build();

        EarnestMoneyOrderResult result = orderAppService.earnestMoneyOrder(cmd);
        assertNotNull(result);
        assertNotNull(result.getOrderNo());
        return result;
    }

    @Test
    @DisplayName("意向金下单应创建 OrderAmount 记录")
    void earnestMoneyOrder_shouldCreateOrderAmount() {
        String accountId = "intg_e2d_user_" + System.currentTimeMillis();
        EarnestMoneyOrderResult result = createAndPayEarnestMoneyOrder(accountId);

        Optional<Order> orderOpt = orderRepository.findByOrderNo(result.getOrderNo());
        assertTrue(orderOpt.isPresent(), "订单应存在");
        Order order = orderOpt.get();

        Optional<OrderAmountPo> amountOpt = orderAmountRepository.findByOrderId(order.getId());
        assertTrue(amountOpt.isPresent(), "OrderAmount 记录应已保存到 vso_order_amount 表");

        OrderAmountPo amount = amountOpt.get();
        assertEquals(order.getId(), amount.getOrderId(), "orderId 应匹配");
        assertNotNull(amount.getAmountId(), "amountId 不应为空");
        // OTD-VSO-REQ-CR-002 §8.1/§8.2 固定金额样例：意向金 5000、定金 20000、已支付 0
        assertEquals(0, new BigDecimal("5000.00").compareTo(amount.getDepositAmount()), "意向金快照应为 5000");
        assertEquals(0, new BigDecimal("20000.00").compareTo(amount.getDownPaymentAmount()), "定金快照应为 20000");
        assertEquals(0, new BigDecimal("200000.00").compareTo(amount.getVehiclePrice()), "车辆价格快照应取选中 Variant");
        assertEquals(0, BigDecimal.ZERO.compareTo(amount.getPaidTotal()), "初始已支付应为 0");
        assertEquals(0, amount.getUpgradedFromSmall(), "升级标记初始应为 0");
    }

    @Test
    @DisplayName("意向金转定金：差额 15000 只创建一个补款任务，重复请求返回同一补款单")
    void earnestMoneyToDownPayment_shouldCreateSingleSupplement() {
        String accountId = "intg_e2d_convert_" + System.currentTimeMillis();
        EarnestMoneyOrderResult orderResult = createAndPayEarnestMoneyOrder(accountId);

        // 模拟意向金支付成功
        orderRepository.findByOrderNo(orderResult.getOrderNo()).ifPresent(order -> {
            order.pay(orderResult.getEarnestMoneyAmount());
            orderRepository.save(order);
        });

        EarnestToDownCmd cmd = EarnestToDownCmd.builder()
                .accountId(accountId)
                .orderNo(orderResult.getOrderNo())
                .customerType("personal")
                .paymentMethod("full_payment")
                .build();

        EarnestToDownResult result = orderAppService.earnestMoneyToDownPayment(cmd);
        assertNotNull(result, "转定金结果不应为空");
        assertNotNull(result.getSupplementaryPayment(), "差额>0 应创建补款任务");
        assertEquals(0, new BigDecimal("15000.00")
                .compareTo(result.getSupplementaryPayment().getAmount().getAmount()), "差额应为 15000");
        String supplementaryNo = result.getSupplementaryPayment().getSupplementaryNo();

        Order afterFirst = orderRepository.findByOrderNo(orderResult.getOrderNo()).orElseThrow();
        assertEquals(OrderState.EARNEST_MONEY_PAID, afterFirst.getOrderState(), "补款未完成前保持意向金已支付");

        // 重复请求：返回同一补款单，不重复创建
        EarnestToDownResult duplicate = orderAppService.earnestMoneyToDownPayment(cmd);
        assertNotNull(duplicate.getSupplementaryPayment());
        assertEquals(supplementaryNo, duplicate.getSupplementaryPayment().getSupplementaryNo(),
                "重复转定金应返回同一补款单");
    }

    @Test
    @DisplayName("意向金不低于定金：直接转定金并标记 upgradedFromSmall")
    void earnestMoneyToDownPayment_shouldDirectConvertWhenDepositNotLess() {
        String accountId = "intg_e2d_direct_" + System.currentTimeMillis();
        EarnestMoneyOrderResult orderResult = createAndPayEarnestMoneyOrder(accountId);

        Order order = orderRepository.findByOrderNo(orderResult.getOrderNo()).orElseThrow();
        order.pay(orderResult.getEarnestMoneyAmount());
        orderRepository.save(order);

        // §8.4 意向金高于定金样例：意向金 20000、定金 10000、已支付 20000
        OrderAmountPo amountPo = orderAmountRepository.findByOrderId(order.getId()).orElseThrow();
        amountPo.setDepositAmount(new BigDecimal("20000.00"));
        amountPo.setDownPaymentAmount(new BigDecimal("10000.00"));
        amountPo.setPaidTotal(new BigDecimal("20000.00"));
        orderAmountRepository.save(amountPo);

        EarnestToDownCmd cmd = EarnestToDownCmd.builder()
                .accountId(accountId)
                .orderNo(orderResult.getOrderNo())
                .customerType("personal")
                .paymentMethod("full_payment")
                .build();

        EarnestToDownResult result = orderAppService.earnestMoneyToDownPayment(cmd);
        assertEquals(OrderType.FORMAL, result.getOrderType(), "订单类型应变为 FORMAL");
        assertEquals(OrderState.DOWN_PAYMENT_PAID, result.getOrderState(), "订单状态应变为 DOWN_PAYMENT_PAID");

        Order converted = orderRepository.findByOrderNo(orderResult.getOrderNo()).orElseThrow();
        assertEquals(OrderType.FORMAL, converted.getOrderType());
        assertEquals(OrderState.DOWN_PAYMENT_PAID, converted.getOrderState());
        assertNotNull(converted.getDownPaymentTime(), "转定金成功应写入 downPaymentTime");

        OrderAmountPo convertedAmount = orderAmountRepository.findByOrderId(converted.getId()).orElseThrow();
        assertEquals(1, convertedAmount.getUpgradedFromSmall(), "意向金高于定金时 upgradedFromSmall 应为 1");
        assertEquals(0, new BigDecimal("20000.00").compareTo(convertedAmount.getPaidTotal()));
    }
}
