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

@SpringBootTest
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
        assertEquals(0, BigDecimal.ZERO.compareTo(amount.getDepositAmount()), "初始定金应为 0");
        assertEquals(0, BigDecimal.ZERO.compareTo(amount.getDownPaymentAmount()), "初始首付应为 0");
    }

    @Test
    @DisplayName("意向金转定金应成功执行（差额<=0 直接转换）")
    void earnestMoneyToDownPayment_shouldSucceed() {
        String accountId = "intg_e2d_convert_" + System.currentTimeMillis();
        EarnestMoneyOrderResult orderResult = createAndPayEarnestMoneyOrder(accountId);

        // 模拟支付成功：手动将订单状态改为 EARNEST_MONEY_PAID
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
        assertEquals(OrderType.FORMAL, result.getOrderType(), "订单类型应变为 FORMAL");
        assertEquals(OrderState.DOWN_PAYMENT_PAID, result.getOrderState(), "订单状态应变为 DOWN_PAYMENT_PAID");

        // 验证数据库中订单状态已更新
        orderRepository.findByOrderNo(orderResult.getOrderNo()).ifPresent(order -> {
            assertEquals(OrderType.FORMAL, order.getOrderType(), "数据库中订单类型应为 FORMAL");
            assertEquals(OrderState.DOWN_PAYMENT_PAID, order.getOrderState(), "数据库中订单状态应为 DOWN_PAYMENT_PAID");
        });
    }
}
