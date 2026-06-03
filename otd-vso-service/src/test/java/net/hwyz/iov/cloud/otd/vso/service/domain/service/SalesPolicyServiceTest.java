package net.hwyz.iov.cloud.otd.vso.service.domain.service;

import net.hwyz.iov.cloud.otd.vso.service.common.exception.ConfigurationNotForSaleException;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.ModelNotForSaleException;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.OptionNotForSaleException;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.VariantNotForSaleException;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.WishlistInvalidReason;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.SaleModelConfigPolicyRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.SaleModelModelPolicyRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.SaleModelOptionPolicyRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.SaleModelVariantPolicyRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelConfigPolicyPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelModelPolicyPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelOptionPolicyPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelVariantPolicyPo;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SalesPolicyService 单元测试")
class SalesPolicyServiceTest {

    @Mock
    private SaleModelModelPolicyRepository modelPolicyRepository;

    @Mock
    private SaleModelVariantPolicyRepository variantPolicyRepository;

    @Mock
    private SaleModelConfigPolicyRepository configPolicyRepository;

    @Mock
    private SaleModelOptionPolicyRepository optionPolicyRepository;

    @InjectMocks
    private SalesPolicyService salesPolicyService;

    private static final String SALE_MODEL_CODE = "TEST_SALE_MODEL";
    private static final String MODEL_CODE = "MODEL_001";
    private static final String VARIANT_CODE = "VARIANT_001";
    private static final String CONFIG_CODE = "CONFIG_001";
    private static final String OPTION_CODE = "OPT_001";
    private static final String REGION_CODE = "REGION_001";

    // ========== validateModelForSale ==========

    @Nested
    @DisplayName("validateModelForSale 方法")
    class ValidateModelForSaleTest {

        @Test
        @DisplayName("策略表为空时应通过校验（ALL全开）")
        void should_pass_when_policy_table_is_empty() {
            when(modelPolicyRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(Collections.emptyList());

            assertDoesNotThrow(() ->
                salesPolicyService.validateModelForSale(SALE_MODEL_CODE, MODEL_CODE));
        }

        @Test
        @DisplayName("Model 在售时应通过校验")
        void should_pass_when_model_is_active() {
            SaleModelModelPolicyPo policy = SaleModelModelPolicyPo.builder()
                .id(1L).saleModelCode(SALE_MODEL_CODE).modelCode(MODEL_CODE)
                .saleStatus("active").build();

            when(modelPolicyRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(Arrays.asList(policy));
            when(modelPolicyRepository.findBySaleModelCodeAndModelCode(SALE_MODEL_CODE, MODEL_CODE))
                .thenReturn(Optional.of(policy));

            assertDoesNotThrow(() ->
                salesPolicyService.validateModelForSale(SALE_MODEL_CODE, MODEL_CODE));
        }

        @Test
        @DisplayName("Model 未配置策略时应抛出异常")
        void should_throw_when_model_not_configured() {
            SaleModelModelPolicyPo policy = SaleModelModelPolicyPo.builder()
                .id(1L).saleModelCode(SALE_MODEL_CODE).modelCode("OTHER_MODEL")
                .saleStatus("active").build();

            when(modelPolicyRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(Arrays.asList(policy));
            when(modelPolicyRepository.findBySaleModelCodeAndModelCode(SALE_MODEL_CODE, MODEL_CODE))
                .thenReturn(Optional.empty());

            assertThrows(ModelNotForSaleException.class, () ->
                salesPolicyService.validateModelForSale(SALE_MODEL_CODE, MODEL_CODE));
        }

        @Test
        @DisplayName("Model 状态为 off_shelf 时应抛出异常")
        void should_throw_when_model_off_shelf() {
            SaleModelModelPolicyPo policy = SaleModelModelPolicyPo.builder()
                .id(1L).saleModelCode(SALE_MODEL_CODE).modelCode(MODEL_CODE)
                .saleStatus("off_shelf").build();

            when(modelPolicyRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(Arrays.asList(policy));
            when(modelPolicyRepository.findBySaleModelCodeAndModelCode(SALE_MODEL_CODE, MODEL_CODE))
                .thenReturn(Optional.of(policy));

            assertThrows(ModelNotForSaleException.class, () ->
                salesPolicyService.validateModelForSale(SALE_MODEL_CODE, MODEL_CODE));
        }
    }

    // ========== validateVariantForSale ==========

    @Nested
    @DisplayName("validateVariantForSale 方法")
    class ValidateVariantForSaleTest {

        @Test
        @DisplayName("策略表为空时应通过校验（ALL全开）")
        void should_pass_when_policy_table_is_empty() {
            when(variantPolicyRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(Collections.emptyList());

            assertDoesNotThrow(() ->
                salesPolicyService.validateVariantForSale(SALE_MODEL_CODE, VARIANT_CODE));
        }

        @Test
        @DisplayName("Variant 在售且有价格时应通过校验")
        void should_pass_when_variant_is_active_with_price() {
            SaleModelVariantPolicyPo policy = SaleModelVariantPolicyPo.builder()
                .id(1L).saleModelCode(SALE_MODEL_CODE).variantCode(VARIANT_CODE)
                .saleStatus("active").variantPrice(BigDecimal.valueOf(200000)).build();

            when(variantPolicyRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(Arrays.asList(policy));
            when(variantPolicyRepository.findBySaleModelCodeAndVariantCode(SALE_MODEL_CODE, VARIANT_CODE))
                .thenReturn(Optional.of(policy));

            assertDoesNotThrow(() ->
                salesPolicyService.validateVariantForSale(SALE_MODEL_CODE, VARIANT_CODE));
        }

        @Test
        @DisplayName("Variant 未配置策略时应抛出异常")
        void should_throw_when_variant_not_configured() {
            when(variantPolicyRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(Arrays.asList(SaleModelVariantPolicyPo.builder()
                    .id(1L).saleModelCode(SALE_MODEL_CODE).variantCode("OTHER").build()));
            when(variantPolicyRepository.findBySaleModelCodeAndVariantCode(SALE_MODEL_CODE, VARIANT_CODE))
                .thenReturn(Optional.empty());

            assertThrows(VariantNotForSaleException.class, () ->
                salesPolicyService.validateVariantForSale(SALE_MODEL_CODE, VARIANT_CODE));
        }

        @Test
        @DisplayName("Variant 价格为 null 时应抛出异常")
        void should_throw_when_variant_price_is_null() {
            SaleModelVariantPolicyPo policy = SaleModelVariantPolicyPo.builder()
                .id(1L).saleModelCode(SALE_MODEL_CODE).variantCode(VARIANT_CODE)
                .saleStatus("active").variantPrice(null).build();

            when(variantPolicyRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(Arrays.asList(policy));
            when(variantPolicyRepository.findBySaleModelCodeAndVariantCode(SALE_MODEL_CODE, VARIANT_CODE))
                .thenReturn(Optional.of(policy));

            assertThrows(VariantNotForSaleException.class, () ->
                salesPolicyService.validateVariantForSale(SALE_MODEL_CODE, VARIANT_CODE));
        }
    }

    // ========== validateConfigurationForSale ==========

    @Nested
    @DisplayName("validateConfigurationForSale 方法")
    class ValidateConfigurationForSaleTest {

        @Test
        @DisplayName("白名单为空时应通过校验（ALL全开）")
        void should_pass_when_whitelist_is_empty() {
            when(configPolicyRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(Collections.emptyList());

            assertDoesNotThrow(() ->
                salesPolicyService.validateConfigurationForSale(SALE_MODEL_CODE, CONFIG_CODE));
        }

        @Test
        @DisplayName("Configuration 在白名单中且状态为 active 时应通过校验")
        void should_pass_when_config_in_whitelist_and_active() {
            SaleModelConfigPolicyPo policy = SaleModelConfigPolicyPo.builder()
                .id(1L).saleModelCode(SALE_MODEL_CODE).configurationCode(CONFIG_CODE)
                .status("active").build();

            when(configPolicyRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(Arrays.asList(policy));
            when(configPolicyRepository.findBySaleModelCodeAndConfigCode(SALE_MODEL_CODE, CONFIG_CODE))
                .thenReturn(Optional.of(policy));

            assertDoesNotThrow(() ->
                salesPolicyService.validateConfigurationForSale(SALE_MODEL_CODE, CONFIG_CODE));
        }

        @Test
        @DisplayName("Configuration 不在白名单中时应抛出异常")
        void should_throw_when_config_not_in_whitelist() {
            when(configPolicyRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(Arrays.asList(SaleModelConfigPolicyPo.builder()
                    .id(1L).saleModelCode(SALE_MODEL_CODE).configurationCode("OTHER").build()));
            when(configPolicyRepository.findBySaleModelCodeAndConfigCode(SALE_MODEL_CODE, CONFIG_CODE))
                .thenReturn(Optional.empty());

            assertThrows(ConfigurationNotForSaleException.class, () ->
                salesPolicyService.validateConfigurationForSale(SALE_MODEL_CODE, CONFIG_CODE));
        }

        @Test
        @DisplayName("Configuration 状态不为 active 时应抛出异常")
        void should_throw_when_config_status_not_active() {
            SaleModelConfigPolicyPo policy = SaleModelConfigPolicyPo.builder()
                .id(1L).saleModelCode(SALE_MODEL_CODE).configurationCode(CONFIG_CODE)
                .status("off_shelf").build();

            when(configPolicyRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(Arrays.asList(policy));
            when(configPolicyRepository.findBySaleModelCodeAndConfigCode(SALE_MODEL_CODE, CONFIG_CODE))
                .thenReturn(Optional.of(policy));

            assertThrows(ConfigurationNotForSaleException.class, () ->
                salesPolicyService.validateConfigurationForSale(SALE_MODEL_CODE, CONFIG_CODE));
        }
    }

    // ========== validateOptionForSale ==========

    @Nested
    @DisplayName("validateOptionForSale 方法")
    class ValidateOptionForSaleTest {

        @Test
        @DisplayName("OptionCode 可售时应通过校验")
        void should_pass_when_option_is_active_and_has_price() {
            SaleModelOptionPolicyPo policy = SaleModelOptionPolicyPo.builder()
                .id(1L).saleModelCode(SALE_MODEL_CODE).optionCode(OPTION_CODE)
                .saleStatus("active").optionPrice(BigDecimal.valueOf(5000)).build();

            when(optionPolicyRepository.findBySaleModelCodeAndOptionCode(SALE_MODEL_CODE, OPTION_CODE))
                .thenReturn(Optional.of(policy));

            assertDoesNotThrow(() ->
                salesPolicyService.validateOptionForSale(SALE_MODEL_CODE, OPTION_CODE, REGION_CODE));
        }

        @Test
        @DisplayName("OptionCode 未配置销售策略时应抛出异常")
        void should_throw_when_option_not_configured() {
            when(optionPolicyRepository.findBySaleModelCodeAndOptionCode(SALE_MODEL_CODE, OPTION_CODE))
                .thenReturn(Optional.empty());

            assertThrows(OptionNotForSaleException.class, () ->
                salesPolicyService.validateOptionForSale(SALE_MODEL_CODE, OPTION_CODE, REGION_CODE));
        }

        @Test
        @DisplayName("OptionCode 销售状态不为 active 时应抛出异常")
        void should_throw_when_option_sale_status_not_active() {
            SaleModelOptionPolicyPo policy = SaleModelOptionPolicyPo.builder()
                .id(1L).saleModelCode(SALE_MODEL_CODE).optionCode(OPTION_CODE)
                .saleStatus("off_shelf").optionPrice(BigDecimal.valueOf(5000)).build();

            when(optionPolicyRepository.findBySaleModelCodeAndOptionCode(SALE_MODEL_CODE, OPTION_CODE))
                .thenReturn(Optional.of(policy));

            assertThrows(OptionNotForSaleException.class, () ->
                salesPolicyService.validateOptionForSale(SALE_MODEL_CODE, OPTION_CODE, REGION_CODE));
        }

        @Test
        @DisplayName("OptionCode 未配置价格时应抛出异常")
        void should_throw_when_option_price_is_null() {
            SaleModelOptionPolicyPo policy = SaleModelOptionPolicyPo.builder()
                .id(1L).saleModelCode(SALE_MODEL_CODE).optionCode(OPTION_CODE)
                .saleStatus("active").optionPrice(null).build();

            when(optionPolicyRepository.findBySaleModelCodeAndOptionCode(SALE_MODEL_CODE, OPTION_CODE))
                .thenReturn(Optional.of(policy));

            assertThrows(OptionNotForSaleException.class, () ->
                salesPolicyService.validateOptionForSale(SALE_MODEL_CODE, OPTION_CODE, REGION_CODE));
        }
    }

    // ========== checkModelForSale ==========

    @Nested
    @DisplayName("checkModelForSale 方法")
    class CheckModelForSaleTest {

        @Test
        @DisplayName("策略表为空时应返回 null（ALL全开）")
        void should_return_null_when_policy_table_is_empty() {
            when(modelPolicyRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(Collections.emptyList());

            assertNull(salesPolicyService.checkModelForSale(SALE_MODEL_CODE, MODEL_CODE));
        }

        @Test
        @DisplayName("Model 在售时应返回 null")
        void should_return_null_when_model_is_active() {
            SaleModelModelPolicyPo policy = SaleModelModelPolicyPo.builder()
                .id(1L).saleModelCode(SALE_MODEL_CODE).modelCode(MODEL_CODE)
                .saleStatus("active").build();

            when(modelPolicyRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(Arrays.asList(policy));
            when(modelPolicyRepository.findBySaleModelCodeAndModelCode(SALE_MODEL_CODE, MODEL_CODE))
                .thenReturn(Optional.of(policy));

            assertNull(salesPolicyService.checkModelForSale(SALE_MODEL_CODE, MODEL_CODE));
        }

        @Test
        @DisplayName("Model 不存在时应返回 MODEL_OFF_SHELF")
        void should_return_model_off_shelf_when_not_found() {
            when(modelPolicyRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(Arrays.asList(SaleModelModelPolicyPo.builder()
                    .id(1L).saleModelCode(SALE_MODEL_CODE).modelCode("OTHER").build()));
            when(modelPolicyRepository.findBySaleModelCodeAndModelCode(SALE_MODEL_CODE, MODEL_CODE))
                .thenReturn(Optional.empty());

            assertEquals(WishlistInvalidReason.MODEL_OFF_SHELF,
                salesPolicyService.checkModelForSale(SALE_MODEL_CODE, MODEL_CODE));
        }

        @Test
        @DisplayName("Model 状态为 off_shelf 时应返回 MODEL_OFF_SHELF")
        void should_return_model_off_shelf_when_status_off_shelf() {
            SaleModelModelPolicyPo policy = SaleModelModelPolicyPo.builder()
                .id(1L).saleModelCode(SALE_MODEL_CODE).modelCode(MODEL_CODE)
                .saleStatus("off_shelf").build();

            when(modelPolicyRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(Arrays.asList(policy));
            when(modelPolicyRepository.findBySaleModelCodeAndModelCode(SALE_MODEL_CODE, MODEL_CODE))
                .thenReturn(Optional.of(policy));

            assertEquals(WishlistInvalidReason.MODEL_OFF_SHELF,
                salesPolicyService.checkModelForSale(SALE_MODEL_CODE, MODEL_CODE));
        }
    }

    // ========== checkVariantForSale ==========

    @Nested
    @DisplayName("checkVariantForSale 方法")
    class CheckVariantForSaleTest {

        @Test
        @DisplayName("策略表为空时应返回 null")
        void should_return_null_when_policy_table_is_empty() {
            when(variantPolicyRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(Collections.emptyList());

            assertNull(salesPolicyService.checkVariantForSale(SALE_MODEL_CODE, VARIANT_CODE));
        }

        @Test
        @DisplayName("Variant 在售且有价格时应返回 null")
        void should_return_null_when_variant_active_with_price() {
            SaleModelVariantPolicyPo policy = SaleModelVariantPolicyPo.builder()
                .id(1L).saleModelCode(SALE_MODEL_CODE).variantCode(VARIANT_CODE)
                .saleStatus("active").variantPrice(BigDecimal.valueOf(200000)).build();

            when(variantPolicyRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(Arrays.asList(policy));
            when(variantPolicyRepository.findBySaleModelCodeAndVariantCode(SALE_MODEL_CODE, VARIANT_CODE))
                .thenReturn(Optional.of(policy));

            assertNull(salesPolicyService.checkVariantForSale(SALE_MODEL_CODE, VARIANT_CODE));
        }

        @Test
        @DisplayName("Variant 不存在时应返回 VARIANT_OFF_SHELF")
        void should_return_variant_off_shelf_when_not_found() {
            when(variantPolicyRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(Arrays.asList(SaleModelVariantPolicyPo.builder()
                    .id(1L).saleModelCode(SALE_MODEL_CODE).variantCode("OTHER").build()));
            when(variantPolicyRepository.findBySaleModelCodeAndVariantCode(SALE_MODEL_CODE, VARIANT_CODE))
                .thenReturn(Optional.empty());

            assertEquals(WishlistInvalidReason.VARIANT_OFF_SHELF,
                salesPolicyService.checkVariantForSale(SALE_MODEL_CODE, VARIANT_CODE));
        }

        @Test
        @DisplayName("Variant 价格为 null 时应返回 VARIANT_OFF_SHELF")
        void should_return_variant_off_shelf_when_price_null() {
            SaleModelVariantPolicyPo policy = SaleModelVariantPolicyPo.builder()
                .id(1L).saleModelCode(SALE_MODEL_CODE).variantCode(VARIANT_CODE)
                .saleStatus("active").variantPrice(null).build();

            when(variantPolicyRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(Arrays.asList(policy));
            when(variantPolicyRepository.findBySaleModelCodeAndVariantCode(SALE_MODEL_CODE, VARIANT_CODE))
                .thenReturn(Optional.of(policy));

            assertEquals(WishlistInvalidReason.VARIANT_OFF_SHELF,
                salesPolicyService.checkVariantForSale(SALE_MODEL_CODE, VARIANT_CODE));
        }
    }

    // ========== checkConfigurationForSale ==========

    @Nested
    @DisplayName("checkConfigurationForSale 方法")
    class CheckConfigurationForSaleTest {

        @Test
        @DisplayName("configurationCode 为 null 时应返回 null")
        void should_return_null_when_config_code_is_null() {
            assertNull(salesPolicyService.checkConfigurationForSale(SALE_MODEL_CODE, null));
        }

        @Test
        @DisplayName("白名单为空时应返回 null")
        void should_return_null_when_whitelist_is_empty() {
            when(configPolicyRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(Collections.emptyList());

            assertNull(salesPolicyService.checkConfigurationForSale(SALE_MODEL_CODE, CONFIG_CODE));
        }

        @Test
        @DisplayName("Configuration 在白名单且 active 时应返回 null")
        void should_return_null_when_config_active() {
            SaleModelConfigPolicyPo policy = SaleModelConfigPolicyPo.builder()
                .id(1L).saleModelCode(SALE_MODEL_CODE).configurationCode(CONFIG_CODE)
                .status("active").build();

            when(configPolicyRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(Arrays.asList(policy));
            when(configPolicyRepository.findBySaleModelCodeAndConfigCode(SALE_MODEL_CODE, CONFIG_CODE))
                .thenReturn(Optional.of(policy));

            assertNull(salesPolicyService.checkConfigurationForSale(SALE_MODEL_CODE, CONFIG_CODE));
        }

        @Test
        @DisplayName("Configuration 不在白名单时应返回 CONFIGURATION_OFF_SHELF")
        void should_return_config_off_shelf_when_not_in_whitelist() {
            when(configPolicyRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(Arrays.asList(SaleModelConfigPolicyPo.builder()
                    .id(1L).saleModelCode(SALE_MODEL_CODE).configurationCode("OTHER").build()));
            when(configPolicyRepository.findBySaleModelCodeAndConfigCode(SALE_MODEL_CODE, CONFIG_CODE))
                .thenReturn(Optional.empty());

            assertEquals(WishlistInvalidReason.CONFIGURATION_OFF_SHELF,
                salesPolicyService.checkConfigurationForSale(SALE_MODEL_CODE, CONFIG_CODE));
        }
    }

    // ========== checkOptionsForSale ==========

    @Nested
    @DisplayName("checkOptionsForSale 方法")
    class CheckOptionsForSaleTest {

        @Test
        @DisplayName("optionCodes 为 null 时应返回 null")
        void should_return_null_when_option_codes_is_null() {
            assertNull(salesPolicyService.checkOptionsForSale(SALE_MODEL_CODE, null));
        }

        @Test
        @DisplayName("optionCodes 为空时应返回 null")
        void should_return_null_when_option_codes_is_empty() {
            assertNull(salesPolicyService.checkOptionsForSale(SALE_MODEL_CODE, Collections.emptyList()));
        }

        @Test
        @DisplayName("所有 OptionCode 可售时应返回 null")
        void should_return_null_when_all_options_active() {
            SaleModelOptionPolicyPo policy = SaleModelOptionPolicyPo.builder()
                .id(1L).saleModelCode(SALE_MODEL_CODE).optionCode(OPTION_CODE)
                .saleStatus("active").optionPrice(BigDecimal.valueOf(5000)).build();

            when(optionPolicyRepository.findBySaleModelCodeAndOptionCode(SALE_MODEL_CODE, OPTION_CODE))
                .thenReturn(Optional.of(policy));

            assertNull(salesPolicyService.checkOptionsForSale(SALE_MODEL_CODE, Arrays.asList(OPTION_CODE)));
        }

        @Test
        @DisplayName("任一 OptionCode 不可售时应返回 OPTION_OFF_SHELF")
        void should_return_option_off_shelf_when_any_option_not_active() {
            SaleModelOptionPolicyPo policy1 = SaleModelOptionPolicyPo.builder()
                .id(1L).saleModelCode(SALE_MODEL_CODE).optionCode("OPT_001")
                .saleStatus("active").optionPrice(BigDecimal.valueOf(1000)).build();

            when(optionPolicyRepository.findBySaleModelCodeAndOptionCode(SALE_MODEL_CODE, "OPT_001"))
                .thenReturn(Optional.of(policy1));
            when(optionPolicyRepository.findBySaleModelCodeAndOptionCode(SALE_MODEL_CODE, "OPT_002"))
                .thenReturn(Optional.empty());

            assertEquals(WishlistInvalidReason.OPTION_OFF_SHELF,
                salesPolicyService.checkOptionsForSale(SALE_MODEL_CODE, Arrays.asList("OPT_001", "OPT_002")));
        }
    }

    // ========== validateOptionsForSale ==========

    @Nested
    @DisplayName("validateOptionsForSale 方法")
    class ValidateOptionsForSaleTest {

        @Test
        @DisplayName("批量校验应逐个校验每个 OptionCode")
        void should_validate_each_option_code() {
            SaleModelOptionPolicyPo policy1 = SaleModelOptionPolicyPo.builder()
                .id(1L).saleModelCode(SALE_MODEL_CODE).optionCode("OPT_001")
                .saleStatus("active").optionPrice(BigDecimal.valueOf(1000)).build();
            SaleModelOptionPolicyPo policy2 = SaleModelOptionPolicyPo.builder()
                .id(2L).saleModelCode(SALE_MODEL_CODE).optionCode("OPT_002")
                .saleStatus("active").optionPrice(BigDecimal.valueOf(2000)).build();

            when(optionPolicyRepository.findBySaleModelCodeAndOptionCode(SALE_MODEL_CODE, "OPT_001"))
                .thenReturn(Optional.of(policy1));
            when(optionPolicyRepository.findBySaleModelCodeAndOptionCode(SALE_MODEL_CODE, "OPT_002"))
                .thenReturn(Optional.of(policy2));

            assertDoesNotThrow(() ->
                salesPolicyService.validateOptionsForSale(SALE_MODEL_CODE, Arrays.asList("OPT_001", "OPT_002"), REGION_CODE));
        }

        @Test
        @DisplayName("批量校验中任一 OptionCode 不可售时应抛出异常")
        void should_throw_when_any_option_not_saleable() {
            SaleModelOptionPolicyPo policy1 = SaleModelOptionPolicyPo.builder()
                .id(1L).saleModelCode(SALE_MODEL_CODE).optionCode("OPT_001")
                .saleStatus("active").optionPrice(BigDecimal.valueOf(1000)).build();

            when(optionPolicyRepository.findBySaleModelCodeAndOptionCode(SALE_MODEL_CODE, "OPT_001"))
                .thenReturn(Optional.of(policy1));
            when(optionPolicyRepository.findBySaleModelCodeAndOptionCode(SALE_MODEL_CODE, "OPT_002"))
                .thenReturn(Optional.empty());

            assertThrows(OptionNotForSaleException.class, () ->
                salesPolicyService.validateOptionsForSale(SALE_MODEL_CODE, Arrays.asList("OPT_001", "OPT_002"), REGION_CODE));
        }
    }

    // ========== getOptionPrice ==========

    @Nested
    @DisplayName("getOptionPrice 方法")
    class GetOptionPriceTest {

        @Test
        @DisplayName("OptionCode 可售时应返回价格")
        void should_return_price_when_option_active() {
            SaleModelOptionPolicyPo policy = SaleModelOptionPolicyPo.builder()
                .id(1L).saleModelCode(SALE_MODEL_CODE).optionCode(OPTION_CODE)
                .saleStatus("active").optionPrice(BigDecimal.valueOf(5000)).build();

            when(optionPolicyRepository.findBySaleModelCodeAndOptionCode(SALE_MODEL_CODE, OPTION_CODE))
                .thenReturn(Optional.of(policy));

            assertEquals(BigDecimal.valueOf(5000),
                salesPolicyService.getOptionPrice(SALE_MODEL_CODE, OPTION_CODE));
        }

        @Test
        @DisplayName("OptionCode 不存在时应返回 0")
        void should_return_zero_when_option_not_found() {
            when(optionPolicyRepository.findBySaleModelCodeAndOptionCode(SALE_MODEL_CODE, OPTION_CODE))
                .thenReturn(Optional.empty());

            assertEquals(BigDecimal.ZERO,
                salesPolicyService.getOptionPrice(SALE_MODEL_CODE, OPTION_CODE));
        }
    }
}
