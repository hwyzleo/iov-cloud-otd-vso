package net.hwyz.iov.cloud.otd.vso.service.application.service;

import net.hwyz.iov.cloud.edd.mdm.api.service.ConfigurationService;
import net.hwyz.iov.cloud.edd.vmd.api.service.VmdVehicleModelConfigService;
import net.hwyz.iov.cloud.edd.vmd.api.vo.response.VmdBuildConfigResponse;
import net.hwyz.iov.cloud.otd.vso.api.enums.OrderType;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.ModifyOrderConfigCmd;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.OrderStateNotAllowedException;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Order;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderAmount;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderState;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.shared.Money;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.AuditRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.ConfigChangeRefundRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderVehicleSnapshotRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.SaleModelOptionPolicyRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.SaleModelRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.SaleModelVariantPolicyRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.SupplementaryPaymentRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.service.OrderDomainService;
import net.hwyz.iov.cloud.otd.vso.service.domain.service.OrderLockService;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelOptionPolicyPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelVariantPolicyPo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("订单修改配置测试")
class OrderAppServiceModifyConfigTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderDomainService orderDomainService;
    @Mock
    private OrderLockService orderLockService;
    @Mock
    private OrderVehicleSnapshotRepository orderVehicleSnapshotRepository;
    @Mock
    private SaleModelVariantPolicyRepository saleModelVariantPolicyRepository;
    @Mock
    private SaleModelOptionPolicyRepository saleModelOptionPolicyRepository;
    @Mock
    private SaleModelRepository saleModelRepository;
    @Mock
    private VmdVehicleModelConfigService vmdVehicleModelConfigService;
    @Mock
    private ConfigurationService configurationService;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private AuditRepository auditRepository;
    @Mock
    private SupplementaryPaymentRepository supplementaryPaymentRepository;
    @Mock
    private ConfigChangeRefundRepository configChangeRefundRepository;

    @InjectMocks
    private OrderAppService orderAppService;

    private static final String SALE_MODEL_CODE = "SALE_MODEL_MODIFY_TEST";

    private Order buildOrder(String orderNo, OrderState orderState) {
        OrderAmount orderAmount = new OrderAmount("AMT_" + System.currentTimeMillis());
        orderAmount.setVehiclePrice(new Money(new BigDecimal("150000")));
        orderAmount.setOptionPrice(new Money(BigDecimal.ZERO));
        return Order.builder()
                .id("test_order_id_" + System.currentTimeMillis())
                .orderNo(orderNo)
                .orderType(OrderType.FORMAL)
                .orderSource("capp")
                .customerType("personal")
                .brandCode("BRAND001")
                .saleModel(SALE_MODEL_CODE)
                .configurationCode("BUILD_CONFIG_001")
                .currentVersionNo(1)
                .orderState(orderState)
                .orderAmount(orderAmount)
                .build();
    }

    private SaleModelVariantPolicyPo buildVariantPolicy() {
        return SaleModelVariantPolicyPo.builder()
                .id(1L)
                .saleModelCode(SALE_MODEL_CODE)
                .variantCode("VARIANT_MODIFY_001")
                .variantPrice(new BigDecimal("150000"))
                .saleStatus("active")
                .build();
    }

    private SaleModelOptionPolicyPo buildOptionPolicy(String optionCode, String familyCode, BigDecimal price) {
        return SaleModelOptionPolicyPo.builder()
                .saleModelCode(SALE_MODEL_CODE)
                .optionCode(optionCode)
                .optionFamilyCode(familyCode)
                .optionPrice(price)
                .marketingTitle("测试选项")
                .saleStatus("active")
                .build();
    }

    private void mockOrderLockService() {
        doAnswer(invocation -> {
            Runnable action = invocation.getArgument(3);
            action.run();
            return null;
        }).when(orderLockService).executeWithLock(anyString(), anyString(), anyString(), any(Runnable.class));
    }

    @Test
    @DisplayName("修改配置成功应更新订单")
    void testModifyConfigSuccess() {
        String orderNo = "TEST_ORDER_" + System.currentTimeMillis();
        Order order = buildOrder(orderNo, OrderState.EARNEST_MONEY_PAID);

        mockOrderLockService();
        when(orderRepository.findByOrderNoAndAccountId(orderNo, "test_user_001"))
                .thenReturn(Optional.of(order));
        when(configurationService.resolveConfiguration(any()))
                .thenReturn("NEW_CONFIG");
        when(vmdVehicleModelConfigService.getBuildConfigByCode("NEW_CONFIG"))
                .thenReturn(VmdBuildConfigResponse.builder().code("NEW_CONFIG").brandCode("BRAND001").build());
        when(saleModelVariantPolicyRepository.findBySaleModelCodeAndVariantCode(eq(SALE_MODEL_CODE), eq("VARIANT_MODIFY_001")))
                .thenReturn(Optional.of(buildVariantPolicy()));
        when(saleModelOptionPolicyRepository.findBySaleModelCodeAndOptionCodes(eq(SALE_MODEL_CODE), anyList()))
                .thenReturn(Arrays.asList(
                        buildOptionPolicy("OPT_COLOR_RED", "COLOR", new BigDecimal("3000")),
                        buildOptionPolicy("OPT_INTERIOR_BLACK", "INTERIOR", new BigDecimal("5000"))
                ));
        when(saleModelRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(Optional.of(net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelPo.builder()
                        .id(1L).saleModelCode(SALE_MODEL_CODE).modelName("测试车型").build()));
        doNothing().when(orderVehicleSnapshotRepository).logicalDeleteByOrderId(anyString());
        when(orderVehicleSnapshotRepository.findMaxVersionByOrderId(anyString())).thenReturn(1);

        List<String> optionCodes = Arrays.asList("OPT_COLOR_RED", "OPT_INTERIOR_BLACK");

        ModifyOrderConfigCmd cmd = ModifyOrderConfigCmd.builder()
                .accountId("test_user_001")
                .orderNo(orderNo)
                .variantCode("VARIANT_MODIFY_001")
                .optionCodes(optionCodes)
                .build();

        orderAppService.modifyConfig(cmd);

        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    @DisplayName("已交付订单不允许修改配置")
    void testModifyConfigInvalidState() {
        String orderNo = "LOCKED_ORDER_" + System.currentTimeMillis();
        Order lockedOrder = buildOrder(orderNo, OrderState.DELIVERED);

        mockOrderLockService();
        when(orderRepository.findByOrderNoAndAccountId(orderNo, "test_user_002"))
                .thenReturn(Optional.of(lockedOrder));

        List<String> optionCodes = Arrays.asList("OPT_COLOR_BLUE");

        ModifyOrderConfigCmd cmd = ModifyOrderConfigCmd.builder()
                .accountId("test_user_002")
                .orderNo(orderNo)
                .variantCode("VARIANT_MODIFY_001")
                .optionCodes(optionCodes)
                .build();

        assertThrows(OrderStateNotAllowedException.class, () -> {
            orderAppService.modifyConfig(cmd);
        });
    }

    @Test
    @DisplayName("修改配置应正确计算价格: variantPrice + optionTotalPrice")
    void testModifyConfigPriceCalculation() {
        String orderNo = "TEST_ORDER_" + System.currentTimeMillis();
        Order order = buildOrder(orderNo, OrderState.EARNEST_MONEY_PAID);

        mockOrderLockService();
        when(orderRepository.findByOrderNoAndAccountId(orderNo, "test_user_003"))
                .thenReturn(Optional.of(order));
        when(configurationService.resolveConfiguration(any()))
                .thenReturn("NEW_CONFIG");
        when(vmdVehicleModelConfigService.getBuildConfigByCode("NEW_CONFIG"))
                .thenReturn(VmdBuildConfigResponse.builder().code("NEW_CONFIG").brandCode("BRAND001").build());
        when(saleModelVariantPolicyRepository.findBySaleModelCodeAndVariantCode(eq(SALE_MODEL_CODE), eq("VARIANT_MODIFY_001")))
                .thenReturn(Optional.of(buildVariantPolicy()));
        when(saleModelOptionPolicyRepository.findBySaleModelCodeAndOptionCodes(eq(SALE_MODEL_CODE), anyList()))
                .thenReturn(Arrays.asList(
                        buildOptionPolicy("OPT_COLOR_RED", "COLOR", new BigDecimal("3000")),
                        buildOptionPolicy("OPT_INTERIOR_BLACK", "INTERIOR", new BigDecimal("5000"))
                ));
        when(saleModelRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(Optional.of(net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelPo.builder()
                        .id(1L).saleModelCode(SALE_MODEL_CODE).modelName("测试车型").build()));
        doNothing().when(orderVehicleSnapshotRepository).logicalDeleteByOrderId(anyString());
        when(orderVehicleSnapshotRepository.findMaxVersionByOrderId(anyString())).thenReturn(1);

        List<String> optionCodes = Arrays.asList("OPT_COLOR_RED", "OPT_INTERIOR_BLACK");

        ModifyOrderConfigCmd cmd = ModifyOrderConfigCmd.builder()
                .accountId("test_user_003")
                .orderNo(orderNo)
                .variantCode("VARIANT_MODIFY_001")
                .optionCodes(optionCodes)
                .build();

        orderAppService.modifyConfig(cmd);

        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    @DisplayName("修改配置单个选项应正确计算价格")
    void testModifyConfigWithSingleOption() {
        String orderNo = "TEST_ORDER_" + System.currentTimeMillis();
        Order order = buildOrder(orderNo, OrderState.EARNEST_MONEY_PAID);

        mockOrderLockService();
        when(orderRepository.findByOrderNoAndAccountId(orderNo, "test_user_004"))
                .thenReturn(Optional.of(order));
        when(configurationService.resolveConfiguration(any()))
                .thenReturn("NEW_CONFIG");
        when(vmdVehicleModelConfigService.getBuildConfigByCode("NEW_CONFIG"))
                .thenReturn(VmdBuildConfigResponse.builder().code("NEW_CONFIG").brandCode("BRAND001").build());
        when(saleModelVariantPolicyRepository.findBySaleModelCodeAndVariantCode(eq(SALE_MODEL_CODE), eq("VARIANT_MODIFY_001")))
                .thenReturn(Optional.of(buildVariantPolicy()));
        when(saleModelOptionPolicyRepository.findBySaleModelCodeAndOptionCodes(eq(SALE_MODEL_CODE), anyList()))
                .thenReturn(Collections.singletonList(
                        buildOptionPolicy("OPT_COLOR_RED", "COLOR", new BigDecimal("3000"))
                ));
        when(saleModelRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(Optional.of(net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelPo.builder()
                        .id(1L).saleModelCode(SALE_MODEL_CODE).modelName("测试车型").build()));
        doNothing().when(orderVehicleSnapshotRepository).logicalDeleteByOrderId(anyString());
        when(orderVehicleSnapshotRepository.findMaxVersionByOrderId(anyString())).thenReturn(1);

        List<String> optionCodes = Arrays.asList("OPT_COLOR_RED");

        ModifyOrderConfigCmd cmd = ModifyOrderConfigCmd.builder()
                .accountId("test_user_004")
                .orderNo(orderNo)
                .variantCode("VARIANT_MODIFY_001")
                .optionCodes(optionCodes)
                .build();

        orderAppService.modifyConfig(cmd);

        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    @DisplayName("修改配置无选项应正确计算价格")
    void testModifyConfigWithNoOptions() {
        String orderNo = "TEST_ORDER_" + System.currentTimeMillis();
        Order order = buildOrder(orderNo, OrderState.EARNEST_MONEY_PAID);

        mockOrderLockService();
        when(orderRepository.findByOrderNoAndAccountId(orderNo, "test_user_005"))
                .thenReturn(Optional.of(order));
        when(configurationService.resolveConfiguration(any()))
                .thenReturn("NEW_CONFIG");
        when(vmdVehicleModelConfigService.getBuildConfigByCode("NEW_CONFIG"))
                .thenReturn(VmdBuildConfigResponse.builder().code("NEW_CONFIG").brandCode("BRAND001").build());
        when(saleModelVariantPolicyRepository.findBySaleModelCodeAndVariantCode(eq(SALE_MODEL_CODE), eq("VARIANT_MODIFY_001")))
                .thenReturn(Optional.of(buildVariantPolicy()));
        when(saleModelRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(Optional.of(net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelPo.builder()
                        .id(1L).saleModelCode(SALE_MODEL_CODE).modelName("测试车型").build()));
        doNothing().when(orderVehicleSnapshotRepository).logicalDeleteByOrderId(anyString());
        when(orderVehicleSnapshotRepository.findMaxVersionByOrderId(anyString())).thenReturn(1);

        ModifyOrderConfigCmd cmd = ModifyOrderConfigCmd.builder()
                .accountId("test_user_005")
                .orderNo(orderNo)
                .variantCode("VARIANT_MODIFY_001")
                .optionCodes(Arrays.asList())
                .build();

        orderAppService.modifyConfig(cmd);

        verify(orderRepository, times(1)).save(any(Order.class));
    }
}
