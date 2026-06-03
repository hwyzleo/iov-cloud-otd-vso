package net.hwyz.iov.cloud.otd.vso.service.application.service;

import net.hwyz.iov.cloud.edd.vmd.api.service.VmdVehicleModelConfigService;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.GetQuoteCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.QuoteResult;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.ConfigurationNotMatchedException;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.SaleModelNotExistException;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.SaleModelRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.SaleModelVariantPolicyRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.service.SalesPolicyService;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelPo;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SaleModelAppService - 报价 getQuote 测试")
class SaleModelAppServiceGetQuoteTest {

    @Mock
    private SaleModelRepository saleModelRepository;
    @Mock
    private SaleModelVariantPolicyRepository saleModelVariantPolicyRepository;
    @Mock
    private SalesPolicyService salesPolicyService;
    @Mock
    private VmdVehicleModelConfigService vmdVehicleModelConfigService;

    @InjectMocks
    private SaleModelAppService saleModelAppService;

    private static final String SALE_MODEL_CODE = "SM001";
    private static final String VARIANT_CODE = "VAR001";
    private static final String OPTION_CODE_1 = "OPT001";
    private static final String OPTION_CODE_2 = "OPT002";
    private static final String CONFIGURATION_CODE = "CONFIG001";

    private SaleModelPo buildActiveSaleModel() {
        return SaleModelPo.builder()
            .id(1L)
            .saleModelCode(SALE_MODEL_CODE)
            .modelName("XX系列")
            .listingStatus("active")
            .build();
    }

    private SaleModelVariantPolicyPo buildVariantPolicy(String variantCode, BigDecimal variantPrice) {
        return SaleModelVariantPolicyPo.builder()
            .id(1L)
            .saleModelCode(SALE_MODEL_CODE)
            .variantCode(variantCode)
            .saleStatus("active")
            .variantPrice(variantPrice)
            .earnestMoneyPrice(BigDecimal.valueOf(5000))
            .downPaymentPrice(BigDecimal.valueOf(20000))
            .build();
    }

    private GetQuoteCmd buildCmd(String variantCode, List<String> optionCodes) {
        return GetQuoteCmd.builder()
            .saleModelCode(SALE_MODEL_CODE)
            .variantCode(variantCode)
            .optionCodes(optionCodes)
            .regionCode("CN")
            .build();
    }

    @Nested
    @DisplayName("参数校验")
    class ParameterValidation {

        @Test
        @DisplayName("销售车型不存在时抛异常")
        void should_throw_when_sale_model_not_found() {
            when(saleModelRepository.findBySaleModelCode(SALE_MODEL_CODE)).thenReturn(java.util.Optional.empty());
            assertThrows(SaleModelNotExistException.class,
                () -> saleModelAppService.getQuote(buildCmd(VARIANT_CODE, Arrays.asList(OPTION_CODE_1))));
        }

        @Test
        @DisplayName("Configuration 不匹配时抛异常")
        void should_throw_when_configuration_not_matched() {
            when(saleModelRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(java.util.Optional.of(buildActiveSaleModel()));
            doNothing().when(salesPolicyService).validateOptionsForSale(eq(SALE_MODEL_CODE), anyList(), eq("CN"));
            when(vmdVehicleModelConfigService.getBuildConfigCodeByOptionCodes(SALE_MODEL_CODE, Arrays.asList(OPTION_CODE_1)))
                .thenReturn(null);

            assertThrows(ConfigurationNotMatchedException.class,
                () -> saleModelAppService.getQuote(buildCmd(VARIANT_CODE, Arrays.asList(OPTION_CODE_1))));
        }
    }

    @Nested
    @DisplayName("报价计算")
    class QuoteCalculation {

        @Test
        @DisplayName("总价 = variantPrice + Σ(optionPrice)")
        void should_calculate_total_price_correctly() {
            when(saleModelRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(java.util.Optional.of(buildActiveSaleModel()));
            doNothing().when(salesPolicyService).validateOptionsForSale(eq(SALE_MODEL_CODE), anyList(), eq("CN"));
            when(vmdVehicleModelConfigService.getBuildConfigCodeByOptionCodes(SALE_MODEL_CODE, Arrays.asList(OPTION_CODE_1, OPTION_CODE_2)))
                .thenReturn(CONFIGURATION_CODE);
            doNothing().when(salesPolicyService).validateConfigurationForSale(SALE_MODEL_CODE, CONFIGURATION_CODE);
            when(saleModelVariantPolicyRepository.findBySaleModelCodeAndVariantCode(SALE_MODEL_CODE, VARIANT_CODE))
                .thenReturn(java.util.Optional.of(buildVariantPolicy(VARIANT_CODE, BigDecimal.valueOf(199900))));
            when(salesPolicyService.getOptionPrice(SALE_MODEL_CODE, OPTION_CODE_1)).thenReturn(BigDecimal.valueOf(3000));
            when(salesPolicyService.getOptionPrice(SALE_MODEL_CODE, OPTION_CODE_2)).thenReturn(BigDecimal.valueOf(5000));

            QuoteResult result = saleModelAppService.getQuote(buildCmd(VARIANT_CODE, Arrays.asList(OPTION_CODE_1, OPTION_CODE_2)));

            assertNotNull(result);
            assertEquals(CONFIGURATION_CODE, result.getConfigurationCode());
            assertEquals(BigDecimal.valueOf(199900), result.getVariantPrice());
            assertEquals(BigDecimal.valueOf(8000), result.getOptionTotalPrice());
            assertEquals(BigDecimal.valueOf(207900), result.getTotalPrice());
            assertEquals(2, result.getOptionPriceBreakdown().size());
        }

        @Test
        @DisplayName("Variant 不存在时，variantPrice 为 0")
        void should_return_zero_variant_price_when_variant_not_found() {
            when(saleModelRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(java.util.Optional.of(buildActiveSaleModel()));
            doNothing().when(salesPolicyService).validateOptionsForSale(eq(SALE_MODEL_CODE), anyList(), eq("CN"));
            when(vmdVehicleModelConfigService.getBuildConfigCodeByOptionCodes(SALE_MODEL_CODE, Arrays.asList(OPTION_CODE_1)))
                .thenReturn(CONFIGURATION_CODE);
            doNothing().when(salesPolicyService).validateConfigurationForSale(SALE_MODEL_CODE, CONFIGURATION_CODE);
            when(saleModelVariantPolicyRepository.findBySaleModelCodeAndVariantCode(SALE_MODEL_CODE, VARIANT_CODE))
                .thenReturn(java.util.Optional.empty());
            when(salesPolicyService.getOptionPrice(SALE_MODEL_CODE, OPTION_CODE_1)).thenReturn(BigDecimal.valueOf(3000));

            QuoteResult result = saleModelAppService.getQuote(buildCmd(VARIANT_CODE, Arrays.asList(OPTION_CODE_1)));

            assertNotNull(result);
            assertEquals(BigDecimal.ZERO, result.getVariantPrice());
            assertEquals(BigDecimal.valueOf(3000), result.getOptionTotalPrice());
            assertEquals(BigDecimal.valueOf(3000), result.getTotalPrice());
        }

        @Test
        @DisplayName("VariantCode 为空时，variantPrice 为 0")
        void should_return_zero_variant_price_when_variant_code_is_null() {
            when(saleModelRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(java.util.Optional.of(buildActiveSaleModel()));
            doNothing().when(salesPolicyService).validateOptionsForSale(eq(SALE_MODEL_CODE), anyList(), eq("CN"));
            when(vmdVehicleModelConfigService.getBuildConfigCodeByOptionCodes(SALE_MODEL_CODE, Arrays.asList(OPTION_CODE_1)))
                .thenReturn(CONFIGURATION_CODE);
            doNothing().when(salesPolicyService).validateConfigurationForSale(SALE_MODEL_CODE, CONFIGURATION_CODE);
            when(salesPolicyService.getOptionPrice(SALE_MODEL_CODE, OPTION_CODE_1)).thenReturn(BigDecimal.valueOf(3000));

            QuoteResult result = saleModelAppService.getQuote(buildCmd(null, Arrays.asList(OPTION_CODE_1)));

            assertNotNull(result);
            assertEquals(BigDecimal.ZERO, result.getVariantPrice());
            assertEquals(BigDecimal.valueOf(3000), result.getOptionTotalPrice());
            assertEquals(BigDecimal.valueOf(3000), result.getTotalPrice());
        }

        @Test
        @DisplayName("Option 列表为空时，optionTotalPrice 为 0")
        void should_return_zero_option_price_when_option_list_is_empty() {
            when(saleModelRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(java.util.Optional.of(buildActiveSaleModel()));
            doNothing().when(salesPolicyService).validateOptionsForSale(eq(SALE_MODEL_CODE), anyList(), eq("CN"));
            when(vmdVehicleModelConfigService.getBuildConfigCodeByOptionCodes(SALE_MODEL_CODE, Collections.emptyList()))
                .thenReturn(CONFIGURATION_CODE);
            doNothing().when(salesPolicyService).validateConfigurationForSale(SALE_MODEL_CODE, CONFIGURATION_CODE);
            when(saleModelVariantPolicyRepository.findBySaleModelCodeAndVariantCode(SALE_MODEL_CODE, VARIANT_CODE))
                .thenReturn(java.util.Optional.of(buildVariantPolicy(VARIANT_CODE, BigDecimal.valueOf(199900))));

            QuoteResult result = saleModelAppService.getQuote(buildCmd(VARIANT_CODE, Collections.emptyList()));

            assertNotNull(result);
            assertEquals(BigDecimal.valueOf(199900), result.getVariantPrice());
            assertEquals(BigDecimal.ZERO, result.getOptionTotalPrice());
            assertEquals(BigDecimal.valueOf(199900), result.getTotalPrice());
            assertTrue(result.getOptionPriceBreakdown().isEmpty());
        }
    }

    @Nested
    @DisplayName("返回结构验证")
    class ResponseStructure {

        @Test
        @DisplayName("返回结果包含所有必要字段")
        void should_return_all_required_fields() {
            when(saleModelRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(java.util.Optional.of(buildActiveSaleModel()));
            doNothing().when(salesPolicyService).validateOptionsForSale(eq(SALE_MODEL_CODE), anyList(), eq("CN"));
            when(vmdVehicleModelConfigService.getBuildConfigCodeByOptionCodes(SALE_MODEL_CODE, Arrays.asList(OPTION_CODE_1)))
                .thenReturn(CONFIGURATION_CODE);
            doNothing().when(salesPolicyService).validateConfigurationForSale(SALE_MODEL_CODE, CONFIGURATION_CODE);
            when(saleModelVariantPolicyRepository.findBySaleModelCodeAndVariantCode(SALE_MODEL_CODE, VARIANT_CODE))
                .thenReturn(java.util.Optional.of(buildVariantPolicy(VARIANT_CODE, BigDecimal.valueOf(199900))));
            when(salesPolicyService.getOptionPrice(SALE_MODEL_CODE, OPTION_CODE_1)).thenReturn(BigDecimal.valueOf(3000));

            QuoteResult result = saleModelAppService.getQuote(buildCmd(VARIANT_CODE, Arrays.asList(OPTION_CODE_1)));

            assertNotNull(result);
            assertNotNull(result.getConfigurationCode());
            assertNotNull(result.getVariantPrice());
            assertNotNull(result.getOptionTotalPrice());
            assertNotNull(result.getTotalPrice());
            assertNotNull(result.getOptionPriceBreakdown());
            assertFalse(result.getOptionPriceBreakdown().isEmpty());
        }

        @Test
        @DisplayName("Option 价格明细包含 optionCode 和 optionPrice")
        void should_contain_option_code_and_price_in_breakdown() {
            when(saleModelRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(java.util.Optional.of(buildActiveSaleModel()));
            doNothing().when(salesPolicyService).validateOptionsForSale(eq(SALE_MODEL_CODE), anyList(), eq("CN"));
            when(vmdVehicleModelConfigService.getBuildConfigCodeByOptionCodes(SALE_MODEL_CODE, Arrays.asList(OPTION_CODE_1)))
                .thenReturn(CONFIGURATION_CODE);
            doNothing().when(salesPolicyService).validateConfigurationForSale(SALE_MODEL_CODE, CONFIGURATION_CODE);
            when(saleModelVariantPolicyRepository.findBySaleModelCodeAndVariantCode(SALE_MODEL_CODE, VARIANT_CODE))
                .thenReturn(java.util.Optional.of(buildVariantPolicy(VARIANT_CODE, BigDecimal.valueOf(199900))));
            when(salesPolicyService.getOptionPrice(SALE_MODEL_CODE, OPTION_CODE_1)).thenReturn(BigDecimal.valueOf(3000));

            QuoteResult result = saleModelAppService.getQuote(buildCmd(VARIANT_CODE, Arrays.asList(OPTION_CODE_1)));

            QuoteResult.OptionPriceItem item = result.getOptionPriceBreakdown().get(0);
            assertEquals(OPTION_CODE_1, item.getOptionCode());
            assertEquals(BigDecimal.valueOf(3000), item.getOptionPrice());
        }
    }
}
