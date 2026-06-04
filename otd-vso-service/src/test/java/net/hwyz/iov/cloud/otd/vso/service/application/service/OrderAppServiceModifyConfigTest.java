package net.hwyz.iov.cloud.otd.vso.service.application.service;

import net.hwyz.iov.cloud.otd.vso.api.enums.OrderType;
import net.hwyz.iov.cloud.otd.vso.service.BaseTest;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.ModifyOrderConfigCmd;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.OrderStateNotAllowedException;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Order;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderState;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.shared.Money;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.SaleModelOptionPolicyRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.SaleModelVariantPolicyRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelOptionPolicyPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelVariantPolicyPo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class OrderAppServiceModifyConfigTest extends BaseTest {

    @Autowired
    private OrderAppService orderAppService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private SaleModelVariantPolicyRepository saleModelVariantPolicyRepository;

    @Autowired
    private SaleModelOptionPolicyRepository saleModelOptionPolicyRepository;

    private String testOrderNo;
    private String lockedOrderNo;
    private String saleModelCode;

    @BeforeEach
    void setUp() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String random = String.valueOf(System.nanoTime());
        saleModelCode = "SALE_MODEL_MODIFY_" + timestamp;
        
        // 创建可修改配置的订单
        testOrderNo = "TEST_ORDER_" + timestamp + "_" + random;
        Order order = Order.builder()
                .id("test_order_id_" + timestamp + "_" + random)
                .orderNo(testOrderNo)
                .orderType(OrderType.FORMAL)
                .orderSource("capp")
                .customerType("personal")
                .brandCode("BRAND001")
                .saleModel(saleModelCode)
                .configurationCode("BUILD_CONFIG_001")
                .currentVersionNo(1)
                .orderState(OrderState.EARNEST_MONEY_PAID)
                .build();
        orderRepository.save(order);
        
        // 创建已锁定的订单
        lockedOrderNo = "LOCKED_ORDER_" + timestamp + "_" + random;
        Order lockedOrder = Order.builder()
                .id("locked_order_id_" + timestamp + "_" + random)
                .orderNo(lockedOrderNo)
                .orderType(OrderType.FORMAL)
                .orderSource("capp")
                .customerType("personal")
                .brandCode("BRAND001")
                .saleModel(saleModelCode)
                .configurationCode("BUILD_CONFIG_001")
                .currentVersionNo(1)
                .orderState(OrderState.DELIVERED)
                .build();
        orderRepository.save(lockedOrder);

        // 创建 Variant 销售策略
        SaleModelVariantPolicyPo variantPolicy = SaleModelVariantPolicyPo.builder()
                .saleModelCode(saleModelCode)
                .variantCode("VARIANT_MODIFY_001")
                .variantPrice(new BigDecimal("150000"))
                .saleStatus("active")
                .build();
        saleModelVariantPolicyRepository.insert(variantPolicy);

        // 创建 Option 销售策略
        SaleModelOptionPolicyPo optionPolicy1 = SaleModelOptionPolicyPo.builder()
                .saleModelCode(saleModelCode)
                .optionCode("OPT_COLOR_RED")
                .optionFamilyCode("COLOR")
                .optionPrice(new BigDecimal("3000"))
                .marketingTitle("红色车漆")
                .saleStatus("active")
                .build();
        saleModelOptionPolicyRepository.save(optionPolicy1);

        SaleModelOptionPolicyPo optionPolicy2 = SaleModelOptionPolicyPo.builder()
                .saleModelCode(saleModelCode)
                .optionCode("OPT_INTERIOR_BLACK")
                .optionFamilyCode("INTERIOR")
                .optionPrice(new BigDecimal("5000"))
                .marketingTitle("黑色内饰")
                .saleStatus("active")
                .build();
        saleModelOptionPolicyRepository.save(optionPolicy2);
    }

    @Test
    void testModifyConfigSuccess() {
        List<String> optionCodes = Arrays.asList("OPT_COLOR_RED", "OPT_INTERIOR_BLACK");
        
        ModifyOrderConfigCmd cmd = ModifyOrderConfigCmd.builder()
                .accountId("test_user_001")
                .orderNo(testOrderNo)
                .variantCode("VARIANT_MODIFY_001")
                .optionCodes(optionCodes)
                .build();
        
        orderAppService.modifyConfig(cmd);
        
        // 验证订单配置已更新
        Order updatedOrder = orderRepository.findByOrderNo(testOrderNo).orElse(null);
        assertNotNull(updatedOrder, "订单应该存在");
    }

    @Test
    void testModifyConfigInvalidState() {
        List<String> optionCodes = Arrays.asList("OPT_COLOR_BLUE");
        
        ModifyOrderConfigCmd cmd = ModifyOrderConfigCmd.builder()
                .accountId("test_user_002")
                .orderNo(lockedOrderNo)
                .variantCode("VARIANT_MODIFY_001")
                .optionCodes(optionCodes)
                .build();
        
        assertThrows(OrderStateNotAllowedException.class, () -> {
            orderAppService.modifyConfig(cmd);
        });
    }

    @Test
    void testModifyConfigPriceCalculation() {
        List<String> optionCodes = Arrays.asList("OPT_COLOR_RED", "OPT_INTERIOR_BLACK");
        
        ModifyOrderConfigCmd cmd = ModifyOrderConfigCmd.builder()
                .accountId("test_user_003")
                .orderNo(testOrderNo)
                .variantCode("VARIANT_MODIFY_001")
                .optionCodes(optionCodes)
                .build();
        
        orderAppService.modifyConfig(cmd);
        
        // 验证订单金额已更新
        Order orderAfter = orderRepository.findByOrderNo(testOrderNo).orElse(null);
        assertNotNull(orderAfter, "订单应该存在");
        
        // 验证价格计算: variantPrice(150000) + optionPrice(3000+5000) = 158000
        Money vehiclePrice = orderAfter.getOrderAmount().getVehiclePrice();
        assertNotNull(vehiclePrice, "车辆价格不应为空");
        assertEquals(0, new BigDecimal("158000").compareTo(vehiclePrice.getAmount()),
                "车辆价格应为 variantPrice + optionTotalPrice");
    }

    @Test
    void testModifyConfigWithSingleOption() {
        List<String> optionCodes = Arrays.asList("OPT_COLOR_RED");
        
        ModifyOrderConfigCmd cmd = ModifyOrderConfigCmd.builder()
                .accountId("test_user_004")
                .orderNo(testOrderNo)
                .variantCode("VARIANT_MODIFY_001")
                .optionCodes(optionCodes)
                .build();
        
        orderAppService.modifyConfig(cmd);
        
        // 验证价格计算: variantPrice(150000) + optionPrice(3000) = 153000
        Order orderAfter = orderRepository.findByOrderNo(testOrderNo).orElse(null);
        assertNotNull(orderAfter, "订单应该存在");
        
        Money vehiclePrice = orderAfter.getOrderAmount().getVehiclePrice();
        assertNotNull(vehiclePrice, "车辆价格不应为空");
        assertEquals(0, new BigDecimal("153000").compareTo(vehiclePrice.getAmount()),
                "车辆价格应为 variantPrice + optionPrice");
    }

    @Test
    void testModifyConfigWithNoOptions() {
        ModifyOrderConfigCmd cmd = ModifyOrderConfigCmd.builder()
                .accountId("test_user_005")
                .orderNo(testOrderNo)
                .variantCode("VARIANT_MODIFY_001")
                .optionCodes(Arrays.asList())
                .build();
        
        orderAppService.modifyConfig(cmd);
        
        // 验证价格计算: variantPrice(150000) + 0 = 150000
        Order orderAfter = orderRepository.findByOrderNo(testOrderNo).orElse(null);
        assertNotNull(orderAfter, "订单应该存在");
        
        Money vehiclePrice = orderAfter.getOrderAmount().getVehiclePrice();
        assertNotNull(vehiclePrice, "车辆价格不应为空");
        assertEquals(0, new BigDecimal("150000").compareTo(vehiclePrice.getAmount()),
                "车辆价格应为 variantPrice");
    }
}
