package net.hwyz.iov.cloud.otd.vso.service.application.service;

import cn.hutool.json.JSONUtil;
import net.hwyz.iov.cloud.otd.vso.api.enums.OrderType;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.OrderDetailResult;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.OrderNotExistException;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Order;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderState;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderAmountRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderPartyRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderVehicleSnapshotRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.SaleModelOptionPolicyRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.SaleModelVariantPolicyRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.service.OrderDomainService;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderAmountPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderVehicleSnapshotPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelOptionPolicyPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelVariantPolicyPo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("订单查询测试")
class OrderAppServiceQueryTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderAmountRepository orderAmountRepository;
    @Mock
    private OrderPartyRepository orderPartyRepository;
    @Mock
    private OrderDomainService orderDomainService;
    @Mock
    private OrderVehicleSnapshotRepository orderVehicleSnapshotRepository;
    @Mock
    private SaleModelVariantPolicyRepository saleModelVariantPolicyRepository;
    @Mock
    private SaleModelOptionPolicyRepository saleModelOptionPolicyRepository;

    @InjectMocks
    private OrderAppService orderAppService;

    private Order buildOrder(String orderId, String orderNo, OrderType orderType, OrderState orderState) {
        return Order.builder()
                .id(orderId)
                .orderNo(orderNo)
                .orderType(orderType)
                .orderSource("capp")
                .customerType("personal")
                .brandCode("BRAND001")
                .saleModel("SALE_MODEL_001")
                .configurationCode("CONFIG_001")
                .currentVersionNo(1)
                .orderState(orderState)
                .build();
    }

    @Test
    @DisplayName("通过orderNo查询应返回订单详情")
    void testGetByOrderNoShouldReturnOrder() {
        String orderId = "order_id_" + System.currentTimeMillis();
        String orderNo = "ORDER_NO_" + System.currentTimeMillis();

        Order mockOrder = buildOrder(orderId, orderNo, OrderType.SMALL, OrderState.EARNEST_MONEY_UNPAID);
        when(orderRepository.findByOrderNo(orderNo)).thenReturn(Optional.of(mockOrder));

        OrderDetailResult result = orderAppService.getByOrderNo(orderNo);

        assertNotNull(result, "查询结果应该不为空");
        assertEquals(orderId, result.getOrderId(), "订单ID应该匹配");
        assertEquals(orderNo, result.getOrderNo(), "订单号应该匹配");
    }

    @Test
    @DisplayName("查询不存在的订单应抛出异常")
    void testGetByOrderNoWithNonExistentOrderShouldThrowException() {
        String nonExistentOrderNo = "NON_EXISTENT_ORDER_" + System.currentTimeMillis();
        when(orderRepository.findByOrderNo(nonExistentOrderNo)).thenReturn(Optional.empty());

        assertThrows(OrderNotExistException.class, () -> {
            orderAppService.getByOrderNo(nonExistentOrderNo);
        }, "查询不存在的订单应该抛出异常");
    }

    @Test
    @DisplayName("传入null应抛出异常")
    void testGetByOrderNoWithNullShouldThrowException() {
        assertThrows(OrderNotExistException.class, () -> {
            orderAppService.getByOrderNo(null);
        }, "传入null应该抛出异常");
    }

    @Test
    @DisplayName("传入空字符串应抛出异常")
    void testGetByOrderNoWithEmptyStringShouldThrowException() {
        assertThrows(OrderNotExistException.class, () -> {
            orderAppService.getByOrderNo("");
        }, "传入空字符串应该抛出异常");
    }

    @Test
    @DisplayName("通过orderId查询应返回订单详情")
    void testGetByIdShouldReturnOrder() {
        String orderId = "order_id_" + System.currentTimeMillis();
        String orderNo = "ORDER_NO_" + System.currentTimeMillis();

        Order mockOrder = buildOrder(orderId, orderNo, OrderType.SMALL, OrderState.EARNEST_MONEY_UNPAID);
        when(orderDomainService.loadOrder(orderId)).thenReturn(mockOrder);

        OrderDetailResult result = orderAppService.getById(orderId);

        assertNotNull(result, "查询结果应该不为空");
        assertEquals(orderId, result.getOrderId(), "订单ID应该匹配");
        assertEquals(orderNo, result.getOrderNo(), "订单号应该匹配");
    }

    @Test
    @DisplayName("getUserOrder应返回包含展示信息的订单详情")
    void testGetUserOrderWithEnrichment() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String saleModelCode = "SALE_MODEL_DETAIL_" + timestamp;
        String variantCode = "VARIANT_DETAIL_" + timestamp;
        String optionCode1 = "OPT_DETAIL_001_" + timestamp;
        String optionCode2 = "OPT_DETAIL_002_" + timestamp;
        String accountId = "test_user_detail_" + timestamp;
        String orderId = "test_order_detail_" + timestamp;
        String orderNo = "ORDER_DETAIL_" + timestamp;

        Order order = Order.builder()
                .id(orderId)
                .orderNo(orderNo)
                .orderType(OrderType.SMALL)
                .orderSource("capp")
                .customerType("personal")
                .brandCode("BRAND001")
                .saleModel(saleModelCode)
                .configurationCode("CONFIG_001")
                .currentVersionNo(1)
                .orderState(OrderState.EARNEST_MONEY_UNPAID)
                .build();

        OrderVehicleSnapshotPo snapshot = new OrderVehicleSnapshotPo();
        snapshot.setSnapshotId("SNAPSHOT_DETAIL_" + timestamp);
        snapshot.setOrderId(orderId);
        snapshot.setSaleModelCode(saleModelCode);
        snapshot.setSaleModelName("测试车型详情_" + timestamp);
        snapshot.setVariantCode(variantCode);
        snapshot.setOptionCodes(JSONUtil.toJsonStr(Arrays.asList(optionCode1, optionCode2)));
        snapshot.setSnapshotVersion(1);

        SaleModelVariantPolicyPo variantPolicy = SaleModelVariantPolicyPo.builder()
                .saleModelCode(saleModelCode)
                .variantCode(variantCode)
                .variantPrice(new BigDecimal("200000"))
                .saleStatus("active")
                .build();

        SaleModelOptionPolicyPo optionPolicy1 = SaleModelOptionPolicyPo.builder()
                .saleModelCode(saleModelCode)
                .optionCode(optionCode1)
                .optionFamilyCode("COLOR")
                .optionPrice(new BigDecimal("8000"))
                .marketingTitle("珍珠白车漆")
                .marketingImage("http://example.com/white.jpg")
                .saleStatus("active")
                .build();

        SaleModelOptionPolicyPo optionPolicy2 = SaleModelOptionPolicyPo.builder()
                .saleModelCode(saleModelCode)
                .optionCode(optionCode2)
                .optionFamilyCode("INTERIOR")
                .optionPrice(new BigDecimal("12000"))
                .marketingTitle("真皮座椅")
                .marketingImage("http://example.com/leather.jpg")
                .saleStatus("active")
                .build();

        when(orderRepository.findByOrderNoAndAccountId(orderNo, accountId))
                .thenReturn(Optional.of(order));
        when(orderAmountRepository.findByOrderId(orderId))
                .thenReturn(Optional.of(buildOrderAmountPo(orderId)));
        when(orderVehicleSnapshotRepository.findByOrderId(orderId))
                .thenReturn(Optional.of(snapshot));
        when(saleModelVariantPolicyRepository.findBySaleModelCodeAndVariantCode(saleModelCode, variantCode))
                .thenReturn(Optional.of(variantPolicy));
        when(saleModelOptionPolicyRepository.findBySaleModelCodeAndOptionCodes(eq(saleModelCode), anyList()))
                .thenReturn(Arrays.asList(optionPolicy1, optionPolicy2));
        when(orderPartyRepository.findByOrderIdAndRole(orderId, "buyer"))
                .thenReturn(Optional.empty());

        OrderDetailResult result = orderAppService.getUserOrder(accountId, orderNo);

        assertNotNull(result, "查询结果不应为空");
        assertEquals(orderNo, result.getOrderNo(), "订单号应匹配");

        assertNotNull(result.getSaleModelImages(), "saleModelImages 不应为空");
        assertEquals(2, result.getSaleModelImages().size(), "应有 2 张图片");
        assertTrue(result.getSaleModelImages().contains("http://example.com/white.jpg"), "应包含白色车漆图片");
        assertTrue(result.getSaleModelImages().contains("http://example.com/leather.jpg"), "应包含真皮座椅图片");

        assertNotNull(result.getSaleModelDesc(), "saleModelDesc 不应为空");
        assertTrue(result.getSaleModelDesc().contains("珍珠白车漆"), "应包含珍珠白车漆");
        assertTrue(result.getSaleModelDesc().contains("真皮座椅"), "应包含真皮座椅");

        assertNotNull(result.getTotalPrice(), "totalPrice 不应为空");
        assertEquals(0, new BigDecimal("220000").compareTo(result.getTotalPrice()),
                "totalPrice 应为 variantPrice + optionTotalPrice");
    }

    @Test
    @DisplayName("getUserOrder无快照时应返回默认值")
    void testGetUserOrderWithoutSnapshot() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String accountId = "test_user_no_snapshot_" + timestamp;
        String orderId = "test_order_no_snapshot_" + timestamp;
        String orderNo = "ORDER_NO_SNAPSHOT_" + timestamp;

        Order order = Order.builder()
                .id(orderId)
                .orderNo(orderNo)
                .orderType(OrderType.SMALL)
                .orderSource("capp")
                .customerType("personal")
                .brandCode("BRAND001")
                .saleModel("SALE_MODEL_001")
                .configurationCode("CONFIG_001")
                .currentVersionNo(1)
                .orderState(OrderState.EARNEST_MONEY_UNPAID)
                .build();

        when(orderRepository.findByOrderNoAndAccountId(orderNo, accountId))
                .thenReturn(Optional.of(order));
        when(orderAmountRepository.findByOrderId(orderId))
                .thenReturn(Optional.of(buildOrderAmountPo(orderId)));
        when(orderVehicleSnapshotRepository.findByOrderId(orderId))
                .thenReturn(Optional.empty());
        when(orderPartyRepository.findByOrderIdAndRole(orderId, "buyer"))
                .thenReturn(Optional.empty());

        OrderDetailResult result = orderAppService.getUserOrder(accountId, orderNo);

        assertNotNull(result, "查询结果不应为空");

        assertTrue(result.getSaleModelImages() == null || result.getSaleModelImages().isEmpty(),
                "没有快照时 saleModelImages 应为空");
        assertTrue(result.getSaleModelDesc() == null || result.getSaleModelDesc().isEmpty(),
                "没有快照时 saleModelDesc 应为空或null");
        assertTrue(result.getTotalPrice() == null || BigDecimal.ZERO.compareTo(result.getTotalPrice()) == 0,
                "没有快照时 totalPrice 应为0或null");
    }

    private OrderAmountPo buildOrderAmountPo(String orderId) {
        OrderAmountPo po = new OrderAmountPo();
        po.setAmountId("AMT_" + System.currentTimeMillis());
        po.setOrderId(orderId);
        po.setGuidePrice(BigDecimal.ZERO);
        po.setVehiclePrice(BigDecimal.ZERO);
        po.setOptionPrice(BigDecimal.ZERO);
        po.setColorMarkup(BigDecimal.ZERO);
        po.setServiceFee(BigDecimal.ZERO);
        po.setPlateServiceFee(BigDecimal.ZERO);
        po.setInsuranceFee(BigDecimal.ZERO);
        po.setDiscountTotal(BigDecimal.ZERO);
        po.setSubsidyTotal(BigDecimal.ZERO);
        po.setFinanceDiscountTotal(BigDecimal.ZERO);
        po.setDealPriceTotal(BigDecimal.ZERO);
        po.setDepositAmount(BigDecimal.ZERO);
        po.setDownPaymentAmount(BigDecimal.ZERO);
        po.setTailPaymentAmount(BigDecimal.ZERO);
        po.setPaidTotal(BigDecimal.ZERO);
        po.setRefundTotal(BigDecimal.ZERO);
        po.setReceivableTotal(BigDecimal.ZERO);
        po.setNetReceivableTotal(BigDecimal.ZERO);
        po.setUnpaidTotal(BigDecimal.ZERO);
        po.setInvoiceAmount(BigDecimal.ZERO);
        po.setCalculationVersion(1);
        return po;
    }
}
