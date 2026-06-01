package net.hwyz.iov.cloud.otd.vso.service.domain.service;

import net.hwyz.iov.cloud.otd.vso.service.domain.repository.SaleModelOptionPolicyRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.MdmProjectionVariantPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelOptionPolicyPo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ConfiguratorService 单元测试")
class ConfiguratorServiceTest {

    @Mock
    private MdmProjectionService mdmProjectionService;

    @Mock
    private SalesPolicyService salesPolicyService;

    @Mock
    private SaleModelOptionPolicyRepository optionPolicyRepository;

    @InjectMocks
    private ConfiguratorService configuratorService;

    private static final String SALE_MODEL_CODE = "TEST_SALE_MODEL";
    private static final String VARIANT_CODE = "VARIANT_001";
    private static final String REGION_CODE = "REGION_001";

    @Nested
    @DisplayName("getConfigurator 方法")
    class GetConfiguratorTest {

        @Test
        @DisplayName("应返回 Variant 和可售的 Option 策略")
        void should_return_variant_and_saleable_options() {
            MdmProjectionVariantPo variant = new MdmProjectionVariantPo();
            variant.setVariantCode(VARIANT_CODE);

            SaleModelOptionPolicyPo policy1 = SaleModelOptionPolicyPo.builder()
                .id(1L).saleModelCode(SALE_MODEL_CODE).optionCode("OPT_001")
                .optionFamilyCode("COLOR").saleStatus("active")
                .optionPrice(BigDecimal.valueOf(5000)).build();
            SaleModelOptionPolicyPo policy2 = SaleModelOptionPolicyPo.builder()
                .id(2L).saleModelCode(SALE_MODEL_CODE).optionCode("OPT_002")
                .optionFamilyCode("COLOR").saleStatus("active")
                .optionPrice(BigDecimal.valueOf(3000)).build();
            SaleModelOptionPolicyPo policy3 = SaleModelOptionPolicyPo.builder()
                .id(3L).saleModelCode(SALE_MODEL_CODE).optionCode("OPT_003")
                .optionFamilyCode("INTERIOR").saleStatus("off_shelf")
                .optionPrice(BigDecimal.valueOf(2000)).build();

            when(mdmProjectionService.getVariant(VARIANT_CODE)).thenReturn(variant);
            when(optionPolicyRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(Arrays.asList(policy1, policy2, policy3));

            ConfiguratorService.ConfiguratorData result = 
                configuratorService.getConfigurator(VARIANT_CODE, SALE_MODEL_CODE, REGION_CODE);

            assertNotNull(result);
            assertEquals(variant, result.getVariant());
            assertEquals(1, result.getFamilyPolicies().size());
            assertTrue(result.getFamilyPolicies().containsKey("COLOR"));
            assertEquals(2, result.getFamilyPolicies().get("COLOR").size());
        }

        @Test
        @DisplayName("过滤掉没有价格的 Option")
        void should_filter_out_options_without_price() {
            MdmProjectionVariantPo variant = new MdmProjectionVariantPo();
            variant.setVariantCode(VARIANT_CODE);

            SaleModelOptionPolicyPo policy1 = SaleModelOptionPolicyPo.builder()
                .id(1L).saleModelCode(SALE_MODEL_CODE).optionCode("OPT_001")
                .optionFamilyCode("COLOR").saleStatus("active")
                .optionPrice(BigDecimal.valueOf(5000)).build();
            SaleModelOptionPolicyPo policy2 = SaleModelOptionPolicyPo.builder()
                .id(2L).saleModelCode(SALE_MODEL_CODE).optionCode("OPT_002")
                .optionFamilyCode("COLOR").saleStatus("active")
                .optionPrice(null).build();

            when(mdmProjectionService.getVariant(VARIANT_CODE)).thenReturn(variant);
            when(optionPolicyRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(Arrays.asList(policy1, policy2));

            ConfiguratorService.ConfiguratorData result = 
                configuratorService.getConfigurator(VARIANT_CODE, SALE_MODEL_CODE, REGION_CODE);

            assertEquals(1, result.getFamilyPolicies().get("COLOR").size());
        }

        @Test
        @DisplayName("过滤掉空的 Family")
        void should_remove_empty_families() {
            MdmProjectionVariantPo variant = new MdmProjectionVariantPo();
            variant.setVariantCode(VARIANT_CODE);

            SaleModelOptionPolicyPo policy1 = SaleModelOptionPolicyPo.builder()
                .id(1L).saleModelCode(SALE_MODEL_CODE).optionCode("OPT_001")
                .optionFamilyCode("COLOR").saleStatus("off_shelf")
                .optionPrice(BigDecimal.valueOf(5000)).build();

            when(mdmProjectionService.getVariant(VARIANT_CODE)).thenReturn(variant);
            when(optionPolicyRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(Arrays.asList(policy1));

            ConfiguratorService.ConfiguratorData result = 
                configuratorService.getConfigurator(VARIANT_CODE, SALE_MODEL_CODE, REGION_CODE);

            assertTrue(result.getFamilyPolicies().isEmpty());
        }

        @Test
        @DisplayName("没有 Option 策略时应返回空 Map")
        void should_return_empty_map_when_no_policies() {
            MdmProjectionVariantPo variant = new MdmProjectionVariantPo();
            variant.setVariantCode(VARIANT_CODE);

            when(mdmProjectionService.getVariant(VARIANT_CODE)).thenReturn(variant);
            when(optionPolicyRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(Collections.emptyList());

            ConfiguratorService.ConfiguratorData result = 
                configuratorService.getConfigurator(VARIANT_CODE, SALE_MODEL_CODE, REGION_CODE);

            assertNotNull(result);
            assertTrue(result.getFamilyPolicies().isEmpty());
        }
    }

    @Nested
    @DisplayName("calculateTotalPrice 方法")
    class CalculateTotalPriceTest {

        @Test
        @DisplayName("应正确计算基础价格加上选项总价")
        void should_calculate_total_price_correctly() {
            BigDecimal basePrice = BigDecimal.valueOf(100000);
            List<String> optionCodes = Arrays.asList("OPT_001", "OPT_002", "OPT_003");

            when(salesPolicyService.getOptionPrice(SALE_MODEL_CODE, "OPT_001"))
                .thenReturn(BigDecimal.valueOf(5000));
            when(salesPolicyService.getOptionPrice(SALE_MODEL_CODE, "OPT_002"))
                .thenReturn(BigDecimal.valueOf(3000));
            when(salesPolicyService.getOptionPrice(SALE_MODEL_CODE, "OPT_003"))
                .thenReturn(BigDecimal.valueOf(2000));

            BigDecimal result = configuratorService.calculateTotalPrice(SALE_MODEL_CODE, basePrice, optionCodes);

            assertEquals(BigDecimal.valueOf(110000), result);
        }

        @Test
        @DisplayName("选项列表为空时应返回基础价格")
        void should_return_base_price_when_no_options() {
            BigDecimal basePrice = BigDecimal.valueOf(100000);

            BigDecimal result = configuratorService.calculateTotalPrice(SALE_MODEL_CODE, basePrice, Collections.emptyList());

            assertEquals(basePrice, result);
        }

        @Test
        @DisplayName("部分选项价格为 0 时应正确计算")
        void should_handle_zero_price_options() {
            BigDecimal basePrice = BigDecimal.valueOf(100000);
            List<String> optionCodes = Arrays.asList("OPT_001", "OPT_002");

            when(salesPolicyService.getOptionPrice(SALE_MODEL_CODE, "OPT_001"))
                .thenReturn(BigDecimal.valueOf(5000));
            when(salesPolicyService.getOptionPrice(SALE_MODEL_CODE, "OPT_002"))
                .thenReturn(BigDecimal.ZERO);

            BigDecimal result = configuratorService.calculateTotalPrice(SALE_MODEL_CODE, basePrice, optionCodes);

            assertEquals(BigDecimal.valueOf(105000), result);
        }
    }
}
