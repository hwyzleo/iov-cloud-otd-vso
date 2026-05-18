package net.hwyz.iov.cloud.otd.vso.service.application.service;

import net.hwyz.iov.cloud.otd.vso.service.BaseTest;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.ModifyOrderConfigCmd;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.OrderStateNotAllowedException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class OrderAppServiceModifyConfigTest extends BaseTest {

    @Autowired
    private OrderAppService orderAppService;

    @Test
    void testModifyConfigSuccess() {
        Map<String, String> featureConfig = new HashMap<>();
        featureConfig.put("COLOR", "RED");
        featureConfig.put("INTERIOR", "BLACK");
        
        ModifyOrderConfigCmd cmd = ModifyOrderConfigCmd.builder()
                .accountId("test_user_001")
                .orderNo("TEST_ORDER_001")
                .featureConfig(featureConfig)
                .build();
        
        orderAppService.modifyConfig(cmd);
    }

    @Test
    void testModifyConfigInvalidState() {
        Map<String, String> featureConfig = new HashMap<>();
        featureConfig.put("COLOR", "BLUE");
        
        ModifyOrderConfigCmd cmd = ModifyOrderConfigCmd.builder()
                .accountId("test_user_002")
                .orderNo("TEST_ORDER_LOCKED")
                .featureConfig(featureConfig)
                .build();
        
        assertThrows(OrderStateNotAllowedException.class, () -> {
            orderAppService.modifyConfig(cmd);
        });
    }
}