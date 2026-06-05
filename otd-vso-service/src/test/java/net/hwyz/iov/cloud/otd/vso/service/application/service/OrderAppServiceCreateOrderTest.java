package net.hwyz.iov.cloud.otd.vso.service.application.service;

import net.hwyz.iov.cloud.edd.mdm.api.service.ConfigurationService;
import net.hwyz.iov.cloud.otd.vso.api.enums.OrderType;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.CreateSmallOrderCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.CreateFormalOrderCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.OrderCreateResult;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Order;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderAmount;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.shared.CustomerInfo;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.shared.VehicleInfo;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.shared.OrganizationInfo;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.SaleModelRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.SaleModelVariantPolicyRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.service.OrderDomainService;
import net.hwyz.iov.cloud.otd.vso.service.domain.service.SalesPolicyService;
import net.hwyz.iov.cloud.otd.vso.service.domain.service.TimeoutNotifyService;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.WishlistRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelVariantPolicyPo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("订单创建测试")
class OrderAppServiceCreateOrderTest {

    @Mock
    private OrderDomainService orderDomainService;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private WishlistRepository wishlistRepository;
    @Mock
    private TimeoutNotifyService timeoutNotifyService;
    @Mock
    private SaleModelRepository saleModelRepository;
    @Mock
    private SalesPolicyService salesPolicyService;
    @Mock
    private ConfigurationService configurationService;
    @Mock
    private SaleModelVariantPolicyRepository saleModelVariantPolicyRepository;

    @InjectMocks
    private OrderAppService orderAppService;

    private Order buildSmallOrder(String orderId, String orderNo) {
        return Order.builder()
                .id(orderId)
                .orderNo(orderNo)
                .orderType(OrderType.SMALL)
                .build();
    }

    private Order buildFormalOrder(String orderId, String orderNo) {
        return Order.builder()
                .id(orderId)
                .orderNo(orderNo)
                .orderType(OrderType.FORMAL)
                .build();
    }

    @Test
    @DisplayName("创建小订单应返回orderNo")
    void testCreateSmallOrderShouldHaveOrderNo() {
        String userId = "test_user_" + System.currentTimeMillis();
        String orderId = "order_id_" + System.currentTimeMillis();
        String orderNo = "ORDER_NO_" + System.currentTimeMillis();

        SaleModelPo saleModelPo = SaleModelPo.builder()
                .id(1L)
                .saleModelCode("SALE_MODEL_001")
                .modelName("测试车型")
                .carlineCode("CARLINE_001")
                .listingStatus("active")
                .build();
        when(saleModelRepository.findBySaleModelCode("SALE_MODEL_001"))
                .thenReturn(Optional.of(saleModelPo));
        when(configurationService.resolveConfiguration(any()))
                .thenReturn("CONFIG_001");

        Order mockOrder = buildSmallOrder(orderId, orderNo);
        when(orderDomainService.createSmallOrder(anyString(), any(CustomerInfo.class), any(VehicleInfo.class)))
                .thenReturn(mockOrder);

        CreateSmallOrderCmd cmd = CreateSmallOrderCmd.builder()
                .orderSource("capp")
                .userId(userId)
                .name("测试用户")
                .mobileHash("mobile_hash")
                .idNoHash("id_no_hash")
                .modelCode("MODEL_001")
                .modelName("测试车型")
                .configCode("CONFIG_001")
                .configName("测试配置")
                .colorCode("COLOR_001")
                .colorName("测试颜色")
                .saleModelCode("SALE_MODEL_001")
                .regionCode("REGION_001")
                .build();

        OrderCreateResult result = orderAppService.createSmallOrder(cmd);

        assertNotNull(result, "返回结果应该不为空");
        assertNotNull(result.getOrderId(), "订单ID应该不为空");
        assertNotNull(result.getOrderNo(), "订单号应该不为空");
        assertEquals(orderNo, result.getOrderNo(), "订单号应该匹配");
        assertEquals(orderId, result.getOrderId(), "订单ID应该匹配");

        verify(orderDomainService, times(1)).createSmallOrder(anyString(), any(CustomerInfo.class), any(VehicleInfo.class));
        verify(wishlistRepository, times(1)).deleteByUserId(userId);
        verify(timeoutNotifyService, times(1)).createTimeoutTask(eq(orderId), eq("SMALL_ORDER_PAY_TIMEOUT"), eq("invalid"), eq(30));
    }

    @Test
    @DisplayName("创建正式订单应返回orderNo")
    void testCreateFormalOrderShouldHaveOrderNo() {
        String userId = "test_user_" + System.currentTimeMillis();
        String orderId = "order_id_" + System.currentTimeMillis();
        String orderNo = "ORDER_NO_" + System.currentTimeMillis();

        SaleModelPo saleModelPo = SaleModelPo.builder()
                .id(1L)
                .saleModelCode("SALE_MODEL_001")
                .modelName("测试车型")
                .carlineCode("CARLINE_001")
                .listingStatus("active")
                .build();
        when(saleModelRepository.findBySaleModelCode("SALE_MODEL_001"))
                .thenReturn(Optional.of(saleModelPo));
        when(configurationService.resolveConfiguration(any()))
                .thenReturn("CONFIG_001");

        // Mock variant policy
        SaleModelVariantPolicyPo variantPolicy = SaleModelVariantPolicyPo.builder()
                .id(1L)
                .saleModelCode("SALE_MODEL_001")
                .variantCode("VARIANT_001")
                .saleStatus("active")
                .variantPrice(new java.math.BigDecimal("150000.00"))
                .build();
        when(saleModelVariantPolicyRepository.findBySaleModelCodeAndVariantCode("SALE_MODEL_001", "VARIANT_001"))
                .thenReturn(Optional.of(variantPolicy));

        Order mockOrder = buildFormalOrder(orderId, orderNo);
        when(orderDomainService.createFormalOrder(anyString(), any(CustomerInfo.class), any(VehicleInfo.class), any(OrganizationInfo.class), any(OrderAmount.class)))
                .thenReturn(mockOrder);

        CreateFormalOrderCmd cmd = CreateFormalOrderCmd.builder()
                .orderSource("capp")
                .userId(userId)
                .name("测试用户")
                .mobileHash("mobile_hash")
                .idNoHash("id_no_hash")
                .modelCode("MODEL_001")
                .modelName("测试车型")
                .configCode("CONFIG_001")
                .configName("测试配置")
                .colorCode("COLOR_001")
                .colorName("测试颜色")
                .saleModelCode("SALE_MODEL_001")
                .variantCode("VARIANT_001")
                .regionCode("REGION_001")
                .ownerRegionCode("REGION_001")
                .ownerRegionName("测试区域")
                .ownerStoreCode("STORE_001")
                .ownerStoreName("测试门店")
                .salesCode("SALES_001")
                .salesName("测试销售")
                .build();

        OrderCreateResult result = orderAppService.createFormalOrder(cmd);

        assertNotNull(result, "返回结果应该不为空");
        assertNotNull(result.getOrderId(), "订单ID应该不为空");
        assertNotNull(result.getOrderNo(), "订单号应该不为空");
        assertEquals(orderNo, result.getOrderNo(), "订单号应该匹配");
        assertEquals(orderId, result.getOrderId(), "订单ID应该匹配");

        verify(orderDomainService, times(1)).createFormalOrder(anyString(), any(CustomerInfo.class), any(VehicleInfo.class), any(OrganizationInfo.class), any(OrderAmount.class));
        verify(timeoutNotifyService, times(1)).createTimeoutTask(eq(orderId), eq("FORMAL_ORDER_AUDIT_TIMEOUT"), eq("remind"), eq(1440));
    }
}
