package net.hwyz.iov.cloud.otd.vso.service.application.service;

import net.hwyz.iov.cloud.edd.mdm.api.service.ConfigurationService;
import net.hwyz.iov.cloud.edd.mdm.api.vo.request.ConfigurationByVariantAndOptionCodesRequest;
import net.hwyz.iov.cloud.edd.mdm.api.vo.response.ConfigurationResponse;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.EarnestMoneyCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.EarnestMoneyOrderResult;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Order;
import net.hwyz.iov.cloud.otd.vso.service.domain.policy.DuplicateOrderSpecification;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.*;
import net.hwyz.iov.cloud.otd.vso.service.domain.service.MdmProjectionService;
import net.hwyz.iov.cloud.otd.vso.service.domain.service.SalesPolicyService;
import net.hwyz.iov.cloud.otd.vso.service.domain.service.TimeoutNotifyService;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.config.PaymentChannelConfig;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderVehicleSnapshotPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelVariantPolicyPo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("意向金下单 - 车辆快照保存测试")
class OrderAppServiceEarnestMoneySnapshotTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderPartyRepository orderPartyRepository;
    @Mock
    private WishlistRepository wishlistRepository;
    @Mock
    private ConfigurationService configurationService;
    @Mock
    private SaleModelRepository saleModelRepository;
    @Mock
    private SaleModelVariantPolicyRepository saleModelVariantPolicyRepository;
    @Mock
    private SaleModelModelPolicyRepository saleModelModelPolicyRepository;
    @Mock
    private SaleModelOptionPolicyRepository optionPolicyRepository;
    @Mock
    private OrderVehicleSnapshotRepository orderVehicleSnapshotRepository;
    @Mock
    private SalesPolicyService salesPolicyService;
    @Mock
    private DuplicateOrderSpecification duplicateOrderSpecification;
    @Mock
    private TimeoutNotifyService timeoutNotifyService;
    @Mock
    private PaymentChannelConfig paymentChannelConfig;
    @Mock
    private MdmProjectionService mdmProjectionService;

    @InjectMocks
    private OrderAppService orderAppService;

    private static final String SALE_MODEL_CODE = "SNAPSHOT_TEST_SM";
    private static final String MODEL_CODE = "SNAPSHOT_TEST_MODEL";
    private static final String VARIANT_CODE = "SNAPSHOT_TEST_VAR";
    private static final String CONFIGURATION_CODE = "SNAPSHOT_TEST_CONFIG";
    private static final String CARLINE_CODE = "CARLINE_001";

    private SaleModelPo buildActiveSaleModel() {
        return SaleModelPo.builder()
                .id(1L)
                .saleModelCode(SALE_MODEL_CODE)
                .modelName("快照测试车型")
                .carlineCode(CARLINE_CODE)
                .listingStatus("active")
                .build();
    }

    private SaleModelVariantPolicyPo buildVariantPolicy() {
        return SaleModelVariantPolicyPo.builder()
                .id(1L)
                .saleModelCode(SALE_MODEL_CODE)
                .variantCode(VARIANT_CODE)
                .saleStatus("active")
                .variantPrice(BigDecimal.valueOf(200000))
                .earnestMoneyPrice(BigDecimal.valueOf(5000))
                .downPaymentPrice(BigDecimal.valueOf(20000))
                .build();
    }

    private EarnestMoneyCmd buildCmd(List<String> optionCodes) {
        return EarnestMoneyCmd.builder()
                .accountId("snapshot_test_user_" + System.currentTimeMillis())
                .saleModel(SALE_MODEL_CODE)
                .modelCode(MODEL_CODE)
                .variantCode(VARIANT_CODE)
                .optionCodes(optionCodes)
                .licenseCityCode("CITY_001")
                .build();
    }

    @Test
    @DisplayName("意向金下单应保存车辆配置快照，且快照字段正确")
    void earnestMoneyOrder_shouldSaveSnapshot_withCorrectFields() {
        List<String> optionCodes = Arrays.asList("OPT_001", "OPT_002");

        when(orderRepository.findByOrderNoAndAccountId(isNull(), anyString()))
                .thenReturn(Optional.empty());
        when(saleModelRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(Optional.of(buildActiveSaleModel()));
        when(configurationService.resolveConfiguration(any(ConfigurationByVariantAndOptionCodesRequest.class)))
                .thenReturn(CONFIGURATION_CODE);
        ConfigurationResponse configResponse = mock(ConfigurationResponse.class);
        when(configResponse.getName()).thenReturn(CONFIGURATION_CODE);
        when(configurationService.getByCode(CONFIGURATION_CODE))
                .thenReturn(configResponse);
        when(saleModelVariantPolicyRepository.findBySaleModelCodeAndVariantCode(SALE_MODEL_CODE, VARIANT_CODE))
                .thenReturn(Optional.of(buildVariantPolicy()));
        when(paymentChannelConfig.getSmallOrderTimeoutMinutes()).thenReturn(30);
        when(salesPolicyService.getOptionPrice(eq(SALE_MODEL_CODE), anyString()))
                .thenReturn(BigDecimal.valueOf(1000));

        // 保存快照时设置 ID
        doAnswer(invocation -> {
            OrderVehicleSnapshotPo po = invocation.getArgument(0);
            po.setId(1L);
            return po;
        }).when(orderVehicleSnapshotRepository).save(any(OrderVehicleSnapshotPo.class));

        EarnestMoneyOrderResult result = orderAppService.earnestMoneyOrder(buildCmd(optionCodes));

        assertNotNull(result, "返回结果应该不为空");
        assertNotNull(result.getOrderNo(), "订单号应该不为空");

        // 捕获快照保存的参数
        ArgumentCaptor<OrderVehicleSnapshotPo> snapshotCaptor = ArgumentCaptor.forClass(OrderVehicleSnapshotPo.class);
        verify(orderVehicleSnapshotRepository, times(1)).save(snapshotCaptor.capture());

        OrderVehicleSnapshotPo snapshot = snapshotCaptor.getValue();

        // 验证核心字段
        assertEquals(CONFIGURATION_CODE, snapshot.getConfigurationCode(),
                "快照的 configurationCode 应与 MDM 解析结果一致");
        assertEquals(CONFIGURATION_CODE, snapshot.getConfigurationName(),
                "快照的 configurationName 应等于 configurationCode");
        assertEquals(SALE_MODEL_CODE, snapshot.getSaleModelCode(),
                "快照的 saleModelCode 应正确");
        assertEquals("快照测试车型", snapshot.getSaleModelName(),
                "快照的 saleModelName 应正确");
        assertEquals(MODEL_CODE, snapshot.getModelCode(),
                "快照的 modelCode 应正确");
        assertEquals(VARIANT_CODE, snapshot.getVariantCode(),
                "快照的 variantCode 应正确");
        assertEquals(CARLINE_CODE, snapshot.getCarlineCode(),
                "快照的 carlineCode 应正确");
        assertNotNull(snapshot.getSnapshotId(), "快照的 snapshotId 不应为空");
        assertEquals(1, snapshot.getSnapshotVersion(), "快照版本应为 1");
        assertNotNull(snapshot.getOptionCodes(), "快照的 optionCodes 不应为空");
        assertTrue(snapshot.getOptionCodes().contains("OPT_001"), "optionCodes 应包含 OPT_001");
        assertTrue(snapshot.getOptionCodes().contains("OPT_002"), "optionCodes 应包含 OPT_002");
        assertNotNull(snapshot.getVariantPolicySnapshot(), "快照的 variantPolicySnapshot 不应为空");
        assertNotNull(snapshot.getOptionBreakdown(), "快照的 optionBreakdown 不应为空");
    }

    @Test
    @DisplayName("意向金下单无 Option 时快照也应正确保存")
    void earnestMoneyOrder_withoutOptions_shouldSaveSnapshot() {
        when(orderRepository.findByOrderNoAndAccountId(isNull(), anyString()))
                .thenReturn(Optional.empty());
        when(saleModelRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(Optional.of(buildActiveSaleModel()));
        when(configurationService.resolveConfiguration(any(ConfigurationByVariantAndOptionCodesRequest.class)))
                .thenReturn(CONFIGURATION_CODE);
        ConfigurationResponse configResponse2 = mock(ConfigurationResponse.class);
        when(configResponse2.getName()).thenReturn(CONFIGURATION_CODE);
        when(configurationService.getByCode(CONFIGURATION_CODE))
                .thenReturn(configResponse2);
        when(saleModelVariantPolicyRepository.findBySaleModelCodeAndVariantCode(SALE_MODEL_CODE, VARIANT_CODE))
                .thenReturn(Optional.of(buildVariantPolicy()));
        when(paymentChannelConfig.getSmallOrderTimeoutMinutes()).thenReturn(30);

        doAnswer(invocation -> {
            OrderVehicleSnapshotPo po = invocation.getArgument(0);
            po.setId(2L);
            return po;
        }).when(orderVehicleSnapshotRepository).save(any(OrderVehicleSnapshotPo.class));

        EarnestMoneyOrderResult result = orderAppService.earnestMoneyOrder(buildCmd(null));

        assertNotNull(result);

        ArgumentCaptor<OrderVehicleSnapshotPo> snapshotCaptor = ArgumentCaptor.forClass(OrderVehicleSnapshotPo.class);
        verify(orderVehicleSnapshotRepository, times(1)).save(snapshotCaptor.capture());

        OrderVehicleSnapshotPo snapshot = snapshotCaptor.getValue();
        assertEquals(CONFIGURATION_CODE, snapshot.getConfigurationCode());
        assertEquals(SALE_MODEL_CODE, snapshot.getSaleModelCode());
    }
}
