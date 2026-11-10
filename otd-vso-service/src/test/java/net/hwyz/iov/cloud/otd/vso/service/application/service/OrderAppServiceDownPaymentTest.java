package net.hwyz.iov.cloud.otd.vso.service.application.service;

import net.hwyz.iov.cloud.edd.mdm.api.service.ConfigurationService;
import net.hwyz.iov.cloud.edd.mdm.api.vo.request.ConfigurationByVariantAndOptionCodesRequest;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.DownPaymentCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.DownPaymentOrderResult;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Order;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderState;
import net.hwyz.iov.cloud.otd.vso.service.domain.policy.DuplicateOrderSpecification;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.ConfigChangeRefundRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.SupplementaryPaymentRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.VehicleAssignmentRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderVehicleSnapshotRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.AuditRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderAssignmentRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.RefundRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.PaymentRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderAmountRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.SaleModelOptionFamilyPolicyRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.SaleModelOptionPolicyRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.SaleModelModelPolicyRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderPartyRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.SaleModelRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.SaleModelVariantPolicyRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.WishlistRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.service.SalesPolicyService;
import net.hwyz.iov.cloud.otd.vso.service.domain.service.TimeoutNotifyService;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.config.PaymentChannelConfig;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelVariantPolicyPo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("定金下单测试")
class OrderAppServiceDownPaymentTest {

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
    private SalesPolicyService salesPolicyService;
    @Mock
    private DuplicateOrderSpecification duplicateOrderSpecification;
    @Mock
    private TimeoutNotifyService timeoutNotifyService;
    @Mock
    private PaymentChannelConfig paymentChannelConfig;

    @Mock
    private PurchaseAgreementAppService purchaseAgreementAppService;
    @Mock
    private OrderTimelineAppService orderTimelineAppService;

    @Mock
    private SaleModelModelPolicyRepository saleModelModelPolicyRepository;
    @Mock
    private SaleModelOptionPolicyRepository optionPolicyRepository;
    @Mock
    private SaleModelOptionFamilyPolicyRepository optionFamilyPolicyRepository;
    @Mock
    private OrderAmountRepository orderAmountRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private RefundRepository refundRepository;
    @Mock
    private OrderAssignmentRepository orderAssignmentRepository;
    @Mock
    private AuditRepository auditRepository;
    @Mock
    private OrderVehicleSnapshotRepository orderVehicleSnapshotRepository;
    @Mock
    private VehicleAssignmentRepository vehicleAssignmentRepository;
    @Mock
    private SupplementaryPaymentRepository supplementaryPaymentRepository;
    @Mock
    private ConfigChangeRefundRepository configChangeRefundRepository;
    @Mock
    private PaymentAppService paymentAppService;
    @Mock
    private RefundAppService refundAppService;
    @Mock
    private DeliveryAppService deliveryAppService;

    @InjectMocks
    private OrderAppService orderAppService;

    private static final String SALE_MODEL_CODE = "DOWN_PAY_TEST_SM";

    private SaleModelVariantPolicyPo buildVariantPolicy() {
        return SaleModelVariantPolicyPo.builder()
                .id(1L)
                .saleModelCode(SALE_MODEL_CODE)
                .variantCode("VAR_001")
                .saleStatus("active")
                .variantPrice(BigDecimal.valueOf(200000))
                .earnestMoneyPrice(BigDecimal.valueOf(5000))
                .downPaymentPrice(BigDecimal.valueOf(20000))
                .build();
    }

    @org.junit.jupiter.api.BeforeEach
    void stubAgreementConfirmations() {
        org.mockito.Mockito.lenient().when(purchaseAgreementAppService.buildConfirmations(
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any()))
                .thenReturn(java.util.List.of());
    }

    @Test
    @DisplayName("定金下单应成功返回订单信息")
    void testDownPaymentOrder() {
        String userId = "test_user_" + System.currentTimeMillis();
        String orderNo = "ORDER_NO_" + System.currentTimeMillis();

        when(saleModelVariantPolicyRepository.findBySaleModelCodeAndVariantCode(SALE_MODEL_CODE, "VAR_001"))
                .thenReturn(Optional.of(buildVariantPolicy()));
        when(paymentChannelConfig.getDownPaymentTimeoutMinutes()).thenReturn(30);

        PaymentChannelConfig.ChannelInfo channelInfo = new PaymentChannelConfig.ChannelInfo();
        channelInfo.setCode(net.hwyz.iov.cloud.otd.vso.api.enums.PaymentChannel.WECHAT);
        channelInfo.setName("微信支付");
        channelInfo.setEnabled(true);

        when(paymentChannelConfig.getDefaultChannelInfo()).thenReturn(channelInfo);
        when(paymentChannelConfig.getEnabledChannels())
                .thenReturn(Collections.singletonList(channelInfo));

        DownPaymentCmd cmd = DownPaymentCmd.builder()
                .accountId(userId)
                .saleModel(SALE_MODEL_CODE)
                .variantCode("VAR_001")
                .configurationCode("CONFIG_001")
                .licenseCityCode("TEST_CITY")
                .build();

        DownPaymentOrderResult result = orderAppService.downPaymentOrder(cmd);

        assertNotNull(result, "返回结果应该不为空");
        assertNotNull(result.getOrderNo(), "订单号应该不为空");
        assertNotNull(result.getDownPaymentAmount(), "定金金额应该不为空");
        assertNotNull(result.getPaymentChannels(), "支付渠道列表应该不为空");
        assertFalse(result.getPaymentChannels().isEmpty(), "支付渠道列表应该不为空");
        assertNotNull(result.getExpireTime(), "过期时间应该不为空");
    }
}
