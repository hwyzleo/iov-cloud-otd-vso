package net.hwyz.iov.cloud.otd.vso.service.application.service;

import cn.hutool.json.JSONUtil;
import net.hwyz.iov.cloud.edd.mdm.api.service.BrandService;
import net.hwyz.iov.cloud.edd.mdm.api.service.CarLineService;
import net.hwyz.iov.cloud.edd.mdm.api.vo.response.BrandResponse;
import net.hwyz.iov.cloud.edd.mdm.api.vo.response.CarLineResponse;
import net.hwyz.iov.cloud.otd.vso.api.enums.OrderType;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.OrderDetailResult;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.OrderNotExistException;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Order;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderState;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.DeliveryRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderAmountRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderTimelineRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.PaymentRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.SaleModelRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderPartyRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderVehicleSnapshotRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.SaleModelOptionPolicyRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.SaleModelVariantPolicyRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.service.OrderDomainService;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderAmountPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderPartyPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderVehicleSnapshotPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelPo;
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
    @Mock
    private SaleModelRepository saleModelRepository;
    @Mock
    private OrderTimelineRepository orderTimelineRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private DeliveryRepository deliveryRepository;
    @Mock
    private CarLineService carLineService;
    @Mock
    private BrandService brandService;

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
    @DisplayName("通过不存在的 orderId 查询详情应抛 ORDER_NOT_EXIST（不得冒泡 IllegalArgumentException）")
    void testGetByIdWithNonExistentOrderShouldThrowOrderNotExist() {
        String orderId = "missing_" + System.currentTimeMillis();
        when(orderDomainService.loadOrder(orderId)).thenThrow(new OrderNotExistException(orderId));

        assertThrows(OrderNotExistException.class, () -> orderAppService.getById(orderId));
    }

    @Test
    @DisplayName("MPT 详情补齐金额快照（意向金/定金）与类型名称")
    void testGetByIdShouldEnrichAmountAndTypeName() {
        String orderId = "order_enrich_" + System.currentTimeMillis();
        Order mockOrder = buildOrder(orderId, "ORDER_ENRICH", OrderType.FORMAL, OrderState.EARNEST_MONEY_PAID);
        when(orderDomainService.loadOrder(orderId)).thenReturn(mockOrder);
        OrderAmountPo amountPo = buildOrderAmountPo(orderId);
        amountPo.setDepositAmount(new BigDecimal("5000.00"));
        amountPo.setDownPaymentAmount(new BigDecimal("20000.00"));
        when(orderAmountRepository.findDomainByOrderId(orderId)).thenReturn(Optional.of(toDomain(amountPo)));

        OrderDetailResult result = orderAppService.getById(orderId);

        assertNotNull(result.getEarnestMoneyAmount(), "意向金金额应由订单金额快照补齐");
        assertEquals(0, new BigDecimal("5000.00").compareTo(result.getEarnestMoneyAmount()));
        assertNotNull(result.getDownPaymentAmount(), "定金金额应由订单金额快照补齐");
        assertEquals(0, new BigDecimal("20000.00").compareTo(result.getDownPaymentAmount()));
        assertEquals("正式订单", result.getOrderTypeName(), "订单类型名称应补齐");
        assertNotNull(result.getOrderSourceName(), "订单来源名称应补齐");
    }

    @Test
    @DisplayName("MPT详情从快照和关联表补齐车型、品牌、购车人、支付状态与金额")
    void testGetByIdShouldEnrichCompleteDetail() {
        String orderId = "ORDER_COMPLETE_DETAIL";
        Order order = Order.builder()
                .id(orderId)
                .orderNo("SO_COMPLETE_DETAIL")
                .orderType(OrderType.FORMAL)
                .orderSource("capp")
                .customerType("personal")
                .paymentMethod("full_payment")
                .purchasePlan(1)
                .brandCode("HC")
                .saleModel("HC")
                .configurationCode("CFG-HC-001")
                .orderState(OrderState.DOWN_PAYMENT_PAID)
                .build();
        when(orderDomainService.loadOrder(orderId)).thenReturn(order);
        OrderAmountPo amountPo = buildOrderAmountPo(orderId);
        amountPo.setVehiclePrice(new BigDecimal("200000.00"));
        amountPo.setOptionPrice(new BigDecimal("10000.00"));
        amountPo.setDealPriceTotal(new BigDecimal("210000.00"));
        amountPo.setPaidTotal(new BigDecimal("5000.00"));
        when(orderAmountRepository.findDomainByOrderId(orderId)).thenReturn(Optional.of(toDomain(amountPo)));

        OrderVehicleSnapshotPo snapshot = new OrderVehicleSnapshotPo();
        snapshot.setOrderId(orderId);
        snapshot.setSaleModelCode("HC");
        snapshot.setModelCode("HC-M1");
        snapshot.setModelName("寒川车型");
        snapshot.setVariantCode("HC-V1");
        snapshot.setVariantName("寒川版");
        snapshot.setConfigurationCode("CFG-HC-001");
        snapshot.setOptionCodes("[]");
        when(orderVehicleSnapshotRepository.findByOrderId(orderId)).thenReturn(Optional.of(snapshot));
        when(saleModelRepository.findBySaleModelCode("HC"))
                .thenReturn(Optional.of(SaleModelPo.builder().saleModelCode("HC").modelName("寒川").build()));
        when(carLineService.getByCode("HC"))
                .thenReturn(CarLineResponse.builder().code("HC").brandCode("BRAND_HC").build());
        when(brandService.getByCode("BRAND_HC"))
                .thenReturn(BrandResponse.builder().code("BRAND_HC").name("测试品牌").build());

        OrderPartyPo buyer = new OrderPartyPo();
        buyer.setPersonType(1);
        buyer.setName("张三");
        buyer.setIdType(1);
        buyer.setIdNoEncrypted("ID-NUM-1");
        when(orderPartyRepository.findByOrderIdAndRole(orderId, "buyer")).thenReturn(Optional.of(buyer));
        when(orderTimelineRepository.findLatestEventTimeByOrderIds(Collections.singletonList(orderId)))
                .thenReturn(Collections.emptyMap());
        when(paymentRepository.findSuccessfulByOrderIds(Collections.singletonList(orderId)))
                .thenReturn(Collections.emptyList());
        when(deliveryRepository.findLatestByOrderId(orderId)).thenReturn(Optional.empty());

        OrderDetailResult result = orderAppService.getById(orderId);

        assertEquals("BRAND_HC", result.getBrandCode());
        assertEquals("测试品牌", result.getBrandName());
        assertEquals("寒川", result.getSaleModelName());
        assertEquals("寒川车型", result.getModelName());
        assertEquals("寒川版", result.getVariantName());
        assertEquals("CFG-HC-001", result.getConfigurationCode());
        assertEquals("personal", result.getCustomerType());
        assertEquals("full_payment", result.getPaymentMethod());
        assertEquals(1, result.getPurchasePlan());
        assertEquals("张三", result.getOrderPersonName());
        assertEquals("ID-NUM-1", result.getOrderPersonIdNum());
        assertEquals(20, result.getPayState(), "直接转定金没有独立定金支付单时也应显示定金已支付");
        assertNotNull(result.getOrderAmount());
        assertEquals(0, new BigDecimal("210000.00").compareTo(
                result.getOrderAmount().getDealPriceTotal().getAmount()));
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
        when(orderAmountRepository.findDomainByOrderId(orderId))
                .thenReturn(Optional.of(toDomain(buildOrderAmountPo(orderId))));
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
        when(orderAmountRepository.findDomainByOrderId(orderId))
                .thenReturn(Optional.of(toDomain(buildOrderAmountPo(orderId))));
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

    private net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderAmount toDomain(
            net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderAmountPo po) {
        return net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.converter.OrderAmountPoConverter.toDomain(po);
    }
}
