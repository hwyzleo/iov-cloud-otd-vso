package net.hwyz.iov.cloud.otd.vso.service.domain.service;

import net.hwyz.iov.cloud.otd.vso.service.common.exception.ConfigurationNotForSaleException;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.OptionNotForSaleException;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.SaleModelConfigPolicyRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.SaleModelOptionPolicyRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelConfigPolicyPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelOptionPolicyPo;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SalesPolicyService 单元测试")
class SalesPolicyServiceTest {

    @Mock
    private SaleModelConfigPolicyRepository configPolicyRepository;

    @Mock
    private SaleModelOptionPolicyRepository optionPolicyRepository;

    @InjectMocks
    private SalesPolicyService salesPolicyService;

    private static final String SALE_MODEL_CODE = "TEST_SALE_MODEL";
    private static final String CONFIG_CODE = "CONFIG_001";
    private static final String OPTION_CODE = "OPT_001";
    private static final String REGION_CODE = "REGION_001";

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
                .id(1L)
                .saleModelCode(SALE_MODEL_CODE)
                .configurationCode(CONFIG_CODE)
                .status("active")
                .build();

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
            SaleModelConfigPolicyPo policy = SaleModelConfigPolicyPo.builder()
                .id(1L)
                .saleModelCode(SALE_MODEL_CODE)
                .configurationCode("OTHER_CONFIG")
                .status("active")
                .build();

            when(configPolicyRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(Arrays.asList(policy));
            when(configPolicyRepository.findBySaleModelCodeAndConfigCode(SALE_MODEL_CODE, CONFIG_CODE))
                .thenReturn(Optional.empty());

            assertThrows(ConfigurationNotForSaleException.class, () -> 
                salesPolicyService.validateConfigurationForSale(SALE_MODEL_CODE, CONFIG_CODE));
        }

        @Test
        @DisplayName("Configuration 状态不为 active 时应抛出异常")
        void should_throw_when_config_status_not_active() {
            SaleModelConfigPolicyPo policy = SaleModelConfigPolicyPo.builder()
                .id(1L)
                .saleModelCode(SALE_MODEL_CODE)
                .configurationCode(CONFIG_CODE)
                .status("off_shelf")
                .build();

            when(configPolicyRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(Arrays.asList(policy));
            when(configPolicyRepository.findBySaleModelCodeAndConfigCode(SALE_MODEL_CODE, CONFIG_CODE))
                .thenReturn(Optional.of(policy));

            assertThrows(ConfigurationNotForSaleException.class, () -> 
                salesPolicyService.validateConfigurationForSale(SALE_MODEL_CODE, CONFIG_CODE));
        }
    }

    @Nested
    @DisplayName("validateOptionForSale 方法")
    class ValidateOptionForSaleTest {

        @Test
        @DisplayName("OptionCode 可售时应通过校验")
        void should_pass_when_option_is_active_and_has_price() {
            SaleModelOptionPolicyPo policy = SaleModelOptionPolicyPo.builder()
                .id(1L)
                .saleModelCode(SALE_MODEL_CODE)
                .optionCode(OPTION_CODE)
                .saleStatus("active")
                .optionPrice(BigDecimal.valueOf(5000))
                .build();

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
                .id(1L)
                .saleModelCode(SALE_MODEL_CODE)
                .optionCode(OPTION_CODE)
                .saleStatus("off_shelf")
                .optionPrice(BigDecimal.valueOf(5000))
                .build();

            when(optionPolicyRepository.findBySaleModelCodeAndOptionCode(SALE_MODEL_CODE, OPTION_CODE))
                .thenReturn(Optional.of(policy));

            assertThrows(OptionNotForSaleException.class, () -> 
                salesPolicyService.validateOptionForSale(SALE_MODEL_CODE, OPTION_CODE, REGION_CODE));
        }

        @Test
        @DisplayName("OptionCode 未配置价格时应抛出异常")
        void should_throw_when_option_price_is_null() {
            SaleModelOptionPolicyPo policy = SaleModelOptionPolicyPo.builder()
                .id(1L)
                .saleModelCode(SALE_MODEL_CODE)
                .optionCode(OPTION_CODE)
                .saleStatus("active")
                .optionPrice(null)
                .build();

            when(optionPolicyRepository.findBySaleModelCodeAndOptionCode(SALE_MODEL_CODE, OPTION_CODE))
                .thenReturn(Optional.of(policy));

            assertThrows(OptionNotForSaleException.class, () -> 
                salesPolicyService.validateOptionForSale(SALE_MODEL_CODE, OPTION_CODE, REGION_CODE));
        }
    }

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

    @Nested
    @DisplayName("getOptionPrice 方法")
    class GetOptionPriceTest {

        @Test
        @DisplayName("OptionCode 可售时应返回价格")
        void should_return_price_when_option_active() {
            SaleModelOptionPolicyPo policy = SaleModelOptionPolicyPo.builder()
                .id(1L)
                .saleModelCode(SALE_MODEL_CODE)
                .optionCode(OPTION_CODE)
                .saleStatus("active")
                .optionPrice(BigDecimal.valueOf(5000))
                .build();

            when(optionPolicyRepository.findBySaleModelCodeAndOptionCode(SALE_MODEL_CODE, OPTION_CODE))
                .thenReturn(Optional.of(policy));

            BigDecimal price = salesPolicyService.getOptionPrice(SALE_MODEL_CODE, OPTION_CODE);
            assertEquals(BigDecimal.valueOf(5000), price);
        }

        @Test
        @DisplayName("OptionCode 不存在时应返回 0")
        void should_return_zero_when_option_not_found() {
            when(optionPolicyRepository.findBySaleModelCodeAndOptionCode(SALE_MODEL_CODE, OPTION_CODE))
                .thenReturn(Optional.empty());

            BigDecimal price = salesPolicyService.getOptionPrice(SALE_MODEL_CODE, OPTION_CODE);
            assertEquals(BigDecimal.ZERO, price);
        }

        @Test
        @DisplayName("OptionCode 状态不为 active 时应返回 0")
        void should_return_zero_when_option_not_active() {
            SaleModelOptionPolicyPo policy = SaleModelOptionPolicyPo.builder()
                .id(1L)
                .saleModelCode(SALE_MODEL_CODE)
                .optionCode(OPTION_CODE)
                .saleStatus("off_shelf")
                .optionPrice(BigDecimal.valueOf(5000))
                .build();

            when(optionPolicyRepository.findBySaleModelCodeAndOptionCode(SALE_MODEL_CODE, OPTION_CODE))
                .thenReturn(Optional.of(policy));

            BigDecimal price = salesPolicyService.getOptionPrice(SALE_MODEL_CODE, OPTION_CODE);
            assertEquals(BigDecimal.ZERO, price);
        }

        @Test
        @DisplayName("OptionCode 价格为 null 时应返回 0")
        void should_return_zero_when_price_is_null() {
            SaleModelOptionPolicyPo policy = SaleModelOptionPolicyPo.builder()
                .id(1L)
                .saleModelCode(SALE_MODEL_CODE)
                .optionCode(OPTION_CODE)
                .saleStatus("active")
                .optionPrice(null)
                .build();

            when(optionPolicyRepository.findBySaleModelCodeAndOptionCode(SALE_MODEL_CODE, OPTION_CODE))
                .thenReturn(Optional.of(policy));

            BigDecimal price = salesPolicyService.getOptionPrice(SALE_MODEL_CODE, OPTION_CODE);
            assertEquals(BigDecimal.ZERO, price);
        }
    }
}
