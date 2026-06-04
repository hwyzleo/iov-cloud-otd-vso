package net.hwyz.iov.cloud.otd.vso.service.application.service;

import net.hwyz.iov.cloud.otd.vso.api.enums.OrderType;
import net.hwyz.iov.cloud.otd.vso.service.BaseTest;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.ModifyOrderConfigCmd;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.OrderStateNotAllowedException;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Order;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderState;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

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

    private String testOrderNo;
    private String lockedOrderNo;

    @BeforeEach
    void setUp() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String random = String.valueOf(System.nanoTime());
        
        // 创建可修改配置的订单
        testOrderNo = "TEST_ORDER_" + timestamp + "_" + random;
        Order order = Order.builder()
                .id("test_order_id_" + timestamp + "_" + random)
                .orderNo(testOrderNo)
                .orderType(OrderType.FORMAL)
                .orderSource("capp")
                .customerType("personal")
                .brandCode("BRAND001")
                .saleModel("SALE_MODEL_001")
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
                .saleModel("SALE_MODEL_001")
                .configurationCode("BUILD_CONFIG_001")
                .currentVersionNo(1)
                .orderState(OrderState.DELIVERED)
                .build();
        orderRepository.save(lockedOrder);
    }

    @Test
    void testModifyConfigSuccess() {
        List<String> optionCodes = Arrays.asList("OPT_COLOR_RED", "OPT_INTERIOR_BLACK");
        
        ModifyOrderConfigCmd cmd = ModifyOrderConfigCmd.builder()
                .accountId("test_user_001")
                .orderNo(testOrderNo)
                .optionCodes(optionCodes)
                .build();
        
        orderAppService.modifyConfig(cmd);
    }

    @Test
    void testModifyConfigInvalidState() {
        List<String> optionCodes = Arrays.asList("OPT_COLOR_BLUE");
        
        ModifyOrderConfigCmd cmd = ModifyOrderConfigCmd.builder()
                .accountId("test_user_002")
                .orderNo(lockedOrderNo)
                .optionCodes(optionCodes)
                .build();
        
        assertThrows(OrderStateNotAllowedException.class, () -> {
            orderAppService.modifyConfig(cmd);
        });
    }
}