package net.hwyz.iov.cloud.otd.vso.service.application.service;

import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.GetConfiguratorCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.ConfiguratorResult;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.SaleModelNotExistException;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.*;
import net.hwyz.iov.cloud.otd.vso.service.domain.service.MdmProjectionService;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.*;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SaleModelAppService - 选配器 getConfigurator 测试")
class SaleModelAppServiceGetConfiguratorTest {

    @Mock
    private SaleModelRepository saleModelRepository;
    @Mock
    private SaleModelModelPolicyRepository saleModelModelPolicyRepository;
    @Mock
    private SaleModelVariantPolicyRepository saleModelVariantPolicyRepository;
    @Mock
    private SaleModelOptionPolicyRepository optionPolicyRepository;
    @Mock
    private SaleModelOptionFamilyPolicyRepository optionFamilyPolicyRepository;
    @Mock
    private MdmProjectionService mdmProjectionService;

    @InjectMocks
    private SaleModelAppService saleModelAppService;

    private static final String SALE_MODEL_CODE = "SM001";
    private static final String CARLINE_CODE = "CAR001";
    private static final String MODEL_CODE_1 = "MOD001";
    private static final String MODEL_CODE_2 = "MOD002";
    private static final String VARIANT_CODE_1 = "VAR001";
    private static final String VARIANT_CODE_2 = "VAR002";
    private static final String OPTION_CODE_1 = "OPT001";
    private static final String OPTION_CODE_2 = "OPT002";
    private static final String OPTION_FAMILY_CODE = "COLOR";

    private SaleModelPo buildActiveSaleModel() {
        return SaleModelPo.builder()
            .id(1L)
            .saleModelCode(SALE_MODEL_CODE)
            .modelName("XX系列")
            .carlineCode(CARLINE_CODE)
            .listingStatus("active")
            .build();
    }

    private SaleModelModelPolicyPo buildModelPolicy(String modelCode, String saleStatus) {
        return SaleModelModelPolicyPo.builder()
            .id(1L)
            .saleModelCode(SALE_MODEL_CODE)
            .modelCode(modelCode)
            .saleStatus(saleStatus)
            .marketingName("标准版")
            .sortWeight(1)
            .build();
    }

    private SaleModelVariantPolicyPo buildVariantPolicy(String modelCode, String variantCode, String saleStatus, BigDecimal price) {
        return SaleModelVariantPolicyPo.builder()
            .id(1L)
            .saleModelCode(SALE_MODEL_CODE)
            .modelCode(modelCode)
            .variantCode(variantCode)
            .saleStatus(saleStatus)
            .variantPrice(price)
            .earnestMoneyPrice(BigDecimal.valueOf(5000))
            .downPaymentPrice(BigDecimal.valueOf(20000))
            .marketingName("后驱长续航")
            .sortWeight(1)
            .build();
    }

    private SaleModelOptionPolicyPo buildOptionPolicy(String modelCode, String variantCode, String optionCode, String familyCode, String saleStatus) {
        return SaleModelOptionPolicyPo.builder()
            .id(1L)
            .saleModelCode(SALE_MODEL_CODE)
            .modelCode(modelCode)
            .variantCode(variantCode)
            .optionCode(optionCode)
            .optionFamilyCode(familyCode)
            .saleStatus(saleStatus)
            .optionPrice(BigDecimal.valueOf(3000))
            .marketingTitle("烈焰红")
            .build();
    }

    private SaleModelOptionFamilyPolicyPo buildOptionFamilyPolicy(String familyCode) {
        return SaleModelOptionFamilyPolicyPo.builder()
            .id(1L)
            .saleModelCode(SALE_MODEL_CODE)
            .optionFamilyCode(familyCode)
            .marketingTitle("外观颜色")
            .sortWeight(1)
            .build();
    }

    private GetConfiguratorCmd buildCmd() {
        return GetConfiguratorCmd.builder()
            .saleModelCode(SALE_MODEL_CODE)
            .regionCode("CN")
            .build();
    }

    @Nested
    @DisplayName("SaleModel 校验")
    class SaleModelValidation {

        @Test
        @DisplayName("销售车型不存在时抛异常")
        void should_throw_when_sale_model_not_found() {
            when(saleModelRepository.findBySaleModelCode(SALE_MODEL_CODE)).thenReturn(java.util.Optional.empty());
            assertThrows(SaleModelNotExistException.class, () -> saleModelAppService.getConfigurator(buildCmd()));
        }

        @Test
        @DisplayName("销售车型已下架时抛异常")
        void should_throw_when_sale_model_off_shelf() {
            SaleModelPo model = buildActiveSaleModel();
            model.setListingStatus("off_shelf");
            when(saleModelRepository.findBySaleModelCode(SALE_MODEL_CODE)).thenReturn(java.util.Optional.of(model));
            assertThrows(SaleModelNotExistException.class, () -> saleModelAppService.getConfigurator(buildCmd()));
        }
    }

    @Nested
    @DisplayName("Model 策略过滤")
    class ModelPolicyFilter {

        @Test
        @DisplayName("Model 策略为空表时，从 MDM 投影获取全部 Model（ALL 全开）")
        void should_return_all_models_when_model_policy_empty() {
            when(saleModelRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(java.util.Optional.of(buildActiveSaleModel()));
            when(saleModelModelPolicyRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(Collections.emptyList());
            when(saleModelVariantPolicyRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(Collections.emptyList());
            when(optionPolicyRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(Collections.emptyList());
            when(optionFamilyPolicyRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(Collections.emptyList());
            when(mdmProjectionService.getModelsByCarlineCode(CARLINE_CODE))
                .thenReturn(Collections.emptyList());

            ConfiguratorResult result = saleModelAppService.getConfigurator(buildCmd());

            assertNotNull(result);
            assertEquals(SALE_MODEL_CODE, result.getSaleModelCode());
            assertTrue(result.getModels().isEmpty());
        }

        @Test
        @DisplayName("Model 策略非空时，过滤 saleStatus!=active 的 Model")
        void should_filter_inactive_models() {
            when(saleModelRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(java.util.Optional.of(buildActiveSaleModel()));
            when(saleModelModelPolicyRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(Arrays.asList(
                    buildModelPolicy(MODEL_CODE_1, "active"),
                    buildModelPolicy(MODEL_CODE_2, "off_shelf")
                ));
            when(saleModelVariantPolicyRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(Arrays.asList(
                    buildVariantPolicy(MODEL_CODE_1, VARIANT_CODE_1, "active", BigDecimal.valueOf(199900))
                ));
            when(optionPolicyRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(Collections.emptyList());
            when(optionFamilyPolicyRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(Collections.emptyList());

            ConfiguratorResult result = saleModelAppService.getConfigurator(buildCmd());

            assertEquals(1, result.getModels().size());
            assertEquals(MODEL_CODE_1, result.getModels().get(0).getModelCode());
        }

        @Test
        @DisplayName("Model 下无可用 Variant 时，该 Model 不返回")
        void should_remove_model_with_no_saleable_variants() {
            when(saleModelRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(java.util.Optional.of(buildActiveSaleModel()));
            when(saleModelModelPolicyRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(Arrays.asList(buildModelPolicy(MODEL_CODE_1, "active")));
            // Variant 策略中该 Model 的 Variant 都不可售
            when(saleModelVariantPolicyRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(Arrays.asList(
                    buildVariantPolicy(MODEL_CODE_1, VARIANT_CODE_1, "off_shelf", BigDecimal.valueOf(199900))
                ));
            when(optionPolicyRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(Collections.emptyList());
            when(optionFamilyPolicyRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(Collections.emptyList());

            ConfiguratorResult result = saleModelAppService.getConfigurator(buildCmd());

            assertTrue(result.getModels().isEmpty());
        }
    }

    @Nested
    @DisplayName("Variant 策略过滤")
    class VariantPolicyFilter {

        @Test
        @DisplayName("过滤 saleStatus!=active 的 Variant")
        void should_filter_inactive_variants() {
            when(saleModelRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(java.util.Optional.of(buildActiveSaleModel()));
            when(saleModelModelPolicyRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(Arrays.asList(buildModelPolicy(MODEL_CODE_1, "active")));
            when(saleModelVariantPolicyRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(Arrays.asList(
                    buildVariantPolicy(MODEL_CODE_1, VARIANT_CODE_1, "active", BigDecimal.valueOf(199900)),
                    buildVariantPolicy(MODEL_CODE_1, VARIANT_CODE_2, "off_shelf", BigDecimal.valueOf(229900))
                ));
            when(optionPolicyRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(Collections.emptyList());
            when(optionFamilyPolicyRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(Collections.emptyList());

            ConfiguratorResult result = saleModelAppService.getConfigurator(buildCmd());

            assertEquals(1, result.getModels().get(0).getVariants().size());
            assertEquals(VARIANT_CODE_1, result.getModels().get(0).getVariants().get(0).getVariantCode());
        }

        @Test
        @DisplayName("过滤 variantPrice 为 null 的 Variant")
        void should_filter_variants_without_price() {
            when(saleModelRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(java.util.Optional.of(buildActiveSaleModel()));
            when(saleModelModelPolicyRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(Arrays.asList(buildModelPolicy(MODEL_CODE_1, "active")));
            when(saleModelVariantPolicyRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(Arrays.asList(
                    buildVariantPolicy(MODEL_CODE_1, VARIANT_CODE_1, "active", null)
                ));
            when(optionPolicyRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(Collections.emptyList());
            when(optionFamilyPolicyRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(Collections.emptyList());

            ConfiguratorResult result = saleModelAppService.getConfigurator(buildCmd());

            // VariantPrice 为 null 的 Variant 被过滤后，该 Model 下无可用 Variant，Model 也不返回
            assertTrue(result.getModels().isEmpty());
        }

        @Test
        @DisplayName("正确设置 Variant 的价格和营销字段")
        void should_set_variant_price_and_marketing_fields() {
            when(saleModelRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(java.util.Optional.of(buildActiveSaleModel()));
            when(saleModelModelPolicyRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(Arrays.asList(buildModelPolicy(MODEL_CODE_1, "active")));
            when(saleModelVariantPolicyRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(Arrays.asList(
                    buildVariantPolicy(MODEL_CODE_1, VARIANT_CODE_1, "active", BigDecimal.valueOf(199900))
                ));
            when(optionPolicyRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(Collections.emptyList());
            when(optionFamilyPolicyRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(Collections.emptyList());

            ConfiguratorResult result = saleModelAppService.getConfigurator(buildCmd());

            ConfiguratorResult.VariantItem variant = result.getModels().get(0).getVariants().get(0);
            assertEquals(VARIANT_CODE_1, variant.getVariantCode());
            assertEquals("后驱长续航", variant.getVariantName());
            assertEquals(BigDecimal.valueOf(199900), variant.getVariantPrice());
            assertEquals(BigDecimal.valueOf(5000), variant.getEarnestMoneyPrice());
            assertEquals(BigDecimal.valueOf(20000), variant.getDownPaymentPrice());
        }
    }

    @Nested
    @DisplayName("Option 策略组装")
    class OptionAssembly {

        @Test
        @DisplayName("Option 按 optionFamilyCode 分组为 selectableFamilies")
        void should_group_options_by_family() {
            when(saleModelRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(java.util.Optional.of(buildActiveSaleModel()));
            when(saleModelModelPolicyRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(Arrays.asList(buildModelPolicy(MODEL_CODE_1, "active")));
            when(saleModelVariantPolicyRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(Arrays.asList(
                    buildVariantPolicy(MODEL_CODE_1, VARIANT_CODE_1, "active", BigDecimal.valueOf(199900))
                ));
            when(optionPolicyRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(Arrays.asList(
                    buildOptionPolicy(MODEL_CODE_1, VARIANT_CODE_1, OPTION_CODE_1, OPTION_FAMILY_CODE, "active"),
                    buildOptionPolicy(MODEL_CODE_1, VARIANT_CODE_1, OPTION_CODE_2, OPTION_FAMILY_CODE, "active")
                ));
            when(optionFamilyPolicyRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(Arrays.asList(buildOptionFamilyPolicy(OPTION_FAMILY_CODE)));

            ConfiguratorResult result = saleModelAppService.getConfigurator(buildCmd());

            ConfiguratorResult.VariantItem variant = result.getModels().get(0).getVariants().get(0);
            assertEquals(1, variant.getSelectableFamilies().size());
            assertEquals(OPTION_FAMILY_CODE, variant.getSelectableFamilies().get(0).getOptionFamilyCode());
            assertEquals("外观颜色", variant.getSelectableFamilies().get(0).getOptionFamilyName());
            assertEquals(2, variant.getSelectableFamilies().get(0).getOptions().size());
        }

        @Test
        @DisplayName("过滤 saleStatus!=active 的 Option")
        void should_filter_inactive_options() {
            when(saleModelRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(java.util.Optional.of(buildActiveSaleModel()));
            when(saleModelModelPolicyRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(Arrays.asList(buildModelPolicy(MODEL_CODE_1, "active")));
            when(saleModelVariantPolicyRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(Arrays.asList(
                    buildVariantPolicy(MODEL_CODE_1, VARIANT_CODE_1, "active", BigDecimal.valueOf(199900))
                ));
            SaleModelOptionPolicyPo activeOption = buildOptionPolicy(MODEL_CODE_1, VARIANT_CODE_1, OPTION_CODE_1, OPTION_FAMILY_CODE, "active");
            SaleModelOptionPolicyPo inactiveOption = buildOptionPolicy(MODEL_CODE_1, VARIANT_CODE_1, OPTION_CODE_2, OPTION_FAMILY_CODE, "off_shelf");
            when(optionPolicyRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(Arrays.asList(activeOption, inactiveOption));
            when(optionFamilyPolicyRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(Arrays.asList(buildOptionFamilyPolicy(OPTION_FAMILY_CODE)));

            ConfiguratorResult result = saleModelAppService.getConfigurator(buildCmd());

            ConfiguratorResult.SelectableFamily family = result.getModels().get(0).getVariants().get(0).getSelectableFamilies().get(0);
            assertEquals(1, family.getOptions().size());
            assertEquals(OPTION_CODE_1, family.getOptions().get(0).getOptionCode());
        }

        @Test
        @DisplayName("Option 价格为 null 时被过滤")
        void should_filter_options_without_price() {
            when(saleModelRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(java.util.Optional.of(buildActiveSaleModel()));
            when(saleModelModelPolicyRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(Arrays.asList(buildModelPolicy(MODEL_CODE_1, "active")));
            when(saleModelVariantPolicyRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(Arrays.asList(
                    buildVariantPolicy(MODEL_CODE_1, VARIANT_CODE_1, "active", BigDecimal.valueOf(199900))
                ));
            SaleModelOptionPolicyPo optionWithPrice = buildOptionPolicy(MODEL_CODE_1, VARIANT_CODE_1, OPTION_CODE_1, OPTION_FAMILY_CODE, "active");
            SaleModelOptionPolicyPo optionNoPrice = buildOptionPolicy(MODEL_CODE_1, VARIANT_CODE_1, OPTION_CODE_2, OPTION_FAMILY_CODE, "active");
            optionNoPrice.setOptionPrice(null);
            when(optionPolicyRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(Arrays.asList(optionWithPrice, optionNoPrice));
            when(optionFamilyPolicyRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(Collections.emptyList());

            ConfiguratorResult result = saleModelAppService.getConfigurator(buildCmd());

            ConfiguratorResult.SelectableFamily family = result.getModels().get(0).getVariants().get(0).getSelectableFamilies().get(0);
            assertEquals(1, family.getOptions().size());
        }
    }

    @Nested
    @DisplayName("返回结构验证")
    class ResponseStructure {

        @Test
        @DisplayName("顶层为 saleModelCode 和 modelName，非 carlineCode/carlineName")
        void should_return_saleModelCode_and_modelName_at_top_level() {
            when(saleModelRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(java.util.Optional.of(buildActiveSaleModel()));
            when(saleModelModelPolicyRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(Collections.emptyList());
            when(saleModelVariantPolicyRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(Collections.emptyList());
            when(optionPolicyRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(Collections.emptyList());
            when(optionFamilyPolicyRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(Collections.emptyList());
            when(mdmProjectionService.getModelsByCarlineCode(CARLINE_CODE))
                .thenReturn(Collections.emptyList());

            ConfiguratorResult result = saleModelAppService.getConfigurator(buildCmd());

            assertEquals(SALE_MODEL_CODE, result.getSaleModelCode());
            assertEquals("XX系列", result.getModelName());
            assertNotNull(result.getModels());
        }
    }
}
