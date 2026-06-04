package net.hwyz.iov.cloud.otd.vso.service.application.service;

import net.hwyz.iov.cloud.edd.mdm.api.service.ConfigurationService;
import net.hwyz.iov.cloud.edd.mdm.api.vo.request.ConfigurationByVariantAndOptionCodesRequest;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.EarnestMoneyCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.EarnestMoneyOrderResult;
import net.hwyz.iov.cloud.otd.vso.service.domain.policy.DuplicateOrderSpecification;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderPartyRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderVehicleSnapshotRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.SaleModelRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.SaleModelVariantPolicyRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.WishlistRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.service.MdmProjectionService;
import net.hwyz.iov.cloud.otd.vso.service.domain.service.SalesPolicyService;
import net.hwyz.iov.cloud.otd.vso.service.domain.service.TimeoutNotifyService;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.config.PaymentChannelConfig;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelVariantPolicyPo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
@DisplayName("意向金下单测试")
class OrderAppServiceEarnestMoneyTest {

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

    private SaleModelPo buildActiveSaleModel(String saleModelCode) {
        return SaleModelPo.builder()
                .id(1L)
                .saleModelCode(saleModelCode)
                .modelName("测试车型")
                .carlineCode("CARLINE_001")
                .listingStatus("active")
                .build();
    }

    private SaleModelVariantPolicyPo buildVariantPolicy(String saleModelCode, String variantCode) {
        return SaleModelVariantPolicyPo.builder()
                .id(1L)
                .saleModelCode(saleModelCode)
                .variantCode(variantCode)
                .saleStatus("active")
                .variantPrice(BigDecimal.valueOf(200000))
                .earnestMoneyPrice(BigDecimal.valueOf(5000))
                .downPaymentPrice(BigDecimal.valueOf(20000))
                .build();
    }

    @Test
    @DisplayName("意向金下单应成功返回订单信息")
    void testEarnestMoneyOrder() {
        String userId = "test_user_" + System.currentTimeMillis();
        String saleModel = "TEST_SALE_CODE";
        String modelCode = "TEST_MODEL";
        String variantCode = "TEST_VARIANT";
        String configurationCode = "TEST_CONFIG";
        List<String> optionCodes = Arrays.asList("OPTION_1", "OPTION_2");

        when(orderRepository.findByOrderNoAndAccountId(isNull(), anyString()))
                .thenReturn(Optional.empty());
        when(saleModelRepository.findBySaleModelCode(saleModel))
                .thenReturn(Optional.of(buildActiveSaleModel(saleModel)));
        when(configurationService.resolveConfiguration(any(ConfigurationByVariantAndOptionCodesRequest.class)))
                .thenReturn(configurationCode);
        when(saleModelVariantPolicyRepository.findBySaleModelCodeAndVariantCode(saleModel, variantCode))
                .thenReturn(Optional.of(buildVariantPolicy(saleModel, variantCode)));
        when(paymentChannelConfig.getSmallOrderTimeoutMinutes()).thenReturn(30);
        when(salesPolicyService.getOptionPrice(eq(saleModel), anyString()))
                .thenReturn(BigDecimal.valueOf(1000));

        EarnestMoneyCmd cmd = EarnestMoneyCmd.builder()
            .accountId(userId)
            .saleModel(saleModel)
            .modelCode(modelCode)
            .variantCode(variantCode)
            .optionCodes(optionCodes)
            .licenseCityCode("TEST_CITY")
            .build();
        
        EarnestMoneyOrderResult result = orderAppService.earnestMoneyOrder(cmd);
        
        assertNotNull(result, "返回结果应该不为空");
        assertNotNull(result.getOrderNo(), "订单号应该不为空");
    }

    @Test
    @DisplayName("意向金直接下单应成功返回订单信息")
    void testEarnestMoneyOrderDirectOrder() {
        String userId = "test_user_direct_" + System.currentTimeMillis();
        String saleModel = "DIRECT_SALE_CODE";
        String variantCode = "DIRECT_VARIANT";
        String configurationCode = "DIRECT_CONFIG";
        List<String> optionCodes = Arrays.asList("DIRECT_OPTION_1");

        when(orderRepository.findByOrderNoAndAccountId(isNull(), anyString()))
                .thenReturn(Optional.empty());
        when(saleModelRepository.findBySaleModelCode(saleModel))
                .thenReturn(Optional.of(buildActiveSaleModel(saleModel)));
        when(configurationService.resolveConfiguration(any(ConfigurationByVariantAndOptionCodesRequest.class)))
                .thenReturn(configurationCode);
        when(saleModelVariantPolicyRepository.findBySaleModelCodeAndVariantCode(saleModel, variantCode))
                .thenReturn(Optional.of(buildVariantPolicy(saleModel, variantCode)));
        when(paymentChannelConfig.getSmallOrderTimeoutMinutes()).thenReturn(30);
        when(salesPolicyService.getOptionPrice(eq(saleModel), anyString()))
                .thenReturn(BigDecimal.valueOf(1000));

        EarnestMoneyCmd cmd = EarnestMoneyCmd.builder()
            .accountId(userId)
            .saleModel(saleModel)
            .modelCode("DIRECT_MODEL")
            .variantCode(variantCode)
            .optionCodes(optionCodes)
            .licenseCityCode("TEST_CITY")
            .build();
        
        EarnestMoneyOrderResult result = orderAppService.earnestMoneyOrder(cmd);
        
        assertNotNull(result, "返回结果应该不为空");
        assertNotNull(result.getOrderNo(), "订单号应该不为空");
    }
}
