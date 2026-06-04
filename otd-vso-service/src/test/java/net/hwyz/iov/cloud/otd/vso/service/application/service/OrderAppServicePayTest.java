package net.hwyz.iov.cloud.otd.vso.service.application.service;

import net.hwyz.iov.cloud.edd.mdm.api.service.ConfigurationService;
import net.hwyz.iov.cloud.edd.mdm.api.vo.request.ConfigurationByVariantAndOptionCodesRequest;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.EarnestMoneyCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.EarnestMoneyOrderResult;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Order;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("意向金下单 - 心愿单清空测试")
class OrderAppServicePayTest {

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

    private static final String SALE_MODEL_CODE = "PAY_TEST_SM";
    private static final String MODEL_CODE = "PAY_TEST_MODEL";
    private static final String VARIANT_CODE = "PAY_TEST_VAR";
    private static final String CONFIGURATION_CODE = "PAY_TEST_CONFIG";

    private SaleModelPo buildActiveSaleModel() {
        return SaleModelPo.builder()
                .id(1L)
                .saleModelCode(SALE_MODEL_CODE)
                .modelName("支付测试车型")
                .carlineCode("CARLINE_001")
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

    @Test
    @DisplayName("意向金下单成功后应清空心愿单")
    void testEarnestMoneyOrderDeletesWishlist() {
        String userId = "test_user_" + System.currentTimeMillis();

        when(orderRepository.findByOrderNoAndAccountId(isNull(), anyString()))
                .thenReturn(Optional.empty());
        when(saleModelRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(Optional.of(buildActiveSaleModel()));
        when(configurationService.resolveConfiguration(any(ConfigurationByVariantAndOptionCodesRequest.class)))
                .thenReturn(CONFIGURATION_CODE);
        when(saleModelVariantPolicyRepository.findBySaleModelCodeAndVariantCode(SALE_MODEL_CODE, VARIANT_CODE))
                .thenReturn(Optional.of(buildVariantPolicy()));
        when(paymentChannelConfig.getSmallOrderTimeoutMinutes()).thenReturn(30);

        EarnestMoneyCmd cmd = EarnestMoneyCmd.builder()
                .accountId(userId)
                .saleModel(SALE_MODEL_CODE)
                .modelCode(MODEL_CODE)
                .variantCode(VARIANT_CODE)
                .licenseCityCode("TEST_CITY")
                .build();

        EarnestMoneyOrderResult result = orderAppService.earnestMoneyOrder(cmd);

        assertNotNull(result, "下单结果应该不为空");
        assertNotNull(result.getOrderNo(), "订单号应该不为空");

        verify(wishlistRepository, times(1)).deleteByUserId(userId);
    }
}
