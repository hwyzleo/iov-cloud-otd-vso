package net.hwyz.iov.cloud.otd.vso.service.application.service;

import net.hwyz.iov.cloud.edd.mdm.api.service.ConfigurationService;
import net.hwyz.iov.cloud.edd.mdm.api.vo.request.ConfigurationByVariantAndOptionCodesRequest;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.EarnestMoneyCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.EarnestMoneyOrderResult;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderVehicleSnapshotRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.OrderVehicleSnapshotMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.SaleModelMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.SaleModelVariantPolicyMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderVehicleSnapshotPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelVariantPolicyPo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
@DisplayName("意向金下单 - 车辆快照集成测试")
class OrderAppServiceEarnestMoneySnapshotIntegrationTest {

    @Autowired
    private OrderAppService orderAppService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderVehicleSnapshotRepository orderVehicleSnapshotRepository;

    @Autowired
    private SaleModelMapper saleModelMapper;

    @Autowired
    private SaleModelVariantPolicyMapper saleModelVariantPolicyMapper;

    @MockBean
    private ConfigurationService configurationService;

    private static final String SALE_MODEL_CODE = "INTG_TEST_SM_" + System.currentTimeMillis();
    private static final String MODEL_CODE = "INTG_TEST_MODEL";
    private static final String VARIANT_CODE = "INTG_TEST_VAR";
    private static final String CONFIGURATION_CODE = "INTG_TEST_CONFIG";
    private static final String CARLINE_CODE = "CARLINE_001";

    private void insertSaleModel() {
        SaleModelPo saleModel = new SaleModelPo();
        saleModel.setSaleModelCode(SALE_MODEL_CODE);
        saleModel.setModelName("集成测试车型");
        saleModel.setCarlineCode(CARLINE_CODE);
        saleModel.setListingStatus("active");
        saleModel.setEnable(true);
        saleModel.setEarnestMoney(true);
        saleModel.setDownPayment(false);
        saleModel.setSort(1);
        saleModelMapper.insertPo(saleModel);
    }

    private void insertVariantPolicy() {
        SaleModelVariantPolicyPo policy = new SaleModelVariantPolicyPo();
        policy.setSaleModelCode(SALE_MODEL_CODE);
        policy.setVariantCode(VARIANT_CODE);
        policy.setSaleStatus("active");
        policy.setVariantPrice(BigDecimal.valueOf(200000));
        policy.setEarnestMoneyPrice(BigDecimal.valueOf(5000));
        policy.setDownPaymentPrice(BigDecimal.valueOf(20000));
        policy.setCreateTime(Timestamp.from(Instant.now()));
        saleModelVariantPolicyMapper.insert(policy);
    }

    @Test
    @DisplayName("意向金下单应正确保存车辆快照到数据库")
    void earnestMoneyOrder_shouldSaveSnapshotToDatabase() {
        insertSaleModel();
        insertVariantPolicy();

        when(configurationService.resolveConfiguration(any(ConfigurationByVariantAndOptionCodesRequest.class)))
                .thenReturn(CONFIGURATION_CODE);

        List<String> optionCodes = null;
        EarnestMoneyCmd cmd = EarnestMoneyCmd.builder()
                .accountId("intg_test_user_" + System.currentTimeMillis())
                .saleModel(SALE_MODEL_CODE)
                .modelCode(MODEL_CODE)
                .variantCode(VARIANT_CODE)
                .optionCodes(optionCodes)
                .licenseCityCode("CITY_001")
                .build();

        EarnestMoneyOrderResult result = orderAppService.earnestMoneyOrder(cmd);

        assertNotNull(result);
        assertNotNull(result.getOrderNo());

        // 从数据库查询订单
        orderRepository.findByOrderNo(result.getOrderNo()).ifPresent(order -> {
            // 从数据库查询快照
            Optional<OrderVehicleSnapshotPo> snapshotOpt = orderVehicleSnapshotRepository.findByOrderId(order.getId());
            assertTrue(snapshotOpt.isPresent(), "快照应已保存到数据库");

            OrderVehicleSnapshotPo snapshot = snapshotOpt.get();
            assertEquals(CONFIGURATION_CODE, snapshot.getConfigurationCode());
            assertEquals(CONFIGURATION_CODE, snapshot.getConfigurationName());
            assertEquals(SALE_MODEL_CODE, snapshot.getSaleModelCode());
            assertEquals("集成测试车型", snapshot.getSaleModelName());
            assertEquals(MODEL_CODE, snapshot.getModelCode());
            assertEquals(VARIANT_CODE, snapshot.getVariantCode());
            assertEquals(CARLINE_CODE, snapshot.getCarlineCode());
            assertNotNull(snapshot.getSnapshotId());
            assertEquals(1, snapshot.getSnapshotVersion());
            assertNotNull(snapshot.getOptionCodes());
            assertNotNull(snapshot.getVariantPolicySnapshot());
            assertNotNull(snapshot.getOptionBreakdown());
        });
    }
}
