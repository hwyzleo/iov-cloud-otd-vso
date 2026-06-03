package net.hwyz.iov.cloud.otd.vso.service.application.service;

import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.SaleModelPriceResult;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.SaleModelVariantPolicyRepository;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SaleModelAppService - 价格查询 getSaleModelPrices 测试")
class SaleModelAppServiceGetSaleModelPricesTest {

    @Mock
    private SaleModelVariantPolicyRepository saleModelVariantPolicyRepository;

    @InjectMocks
    private SaleModelAppService saleModelAppService;

    private static final String SALE_MODEL_CODE = "SM001";

    private SaleModelVariantPolicyPo buildVariantPolicy(String variantCode, BigDecimal variantPrice,
                                                         BigDecimal earnestMoneyPrice, BigDecimal downPaymentPrice,
                                                         String saleStatus, String availableRegions) {
        return SaleModelVariantPolicyPo.builder()
            .id(1L)
            .saleModelCode(SALE_MODEL_CODE)
            .variantCode(variantCode)
            .saleStatus(saleStatus)
            .variantPrice(variantPrice)
            .earnestMoneyPrice(earnestMoneyPrice)
            .downPaymentPrice(downPaymentPrice)
            .availableRegions(availableRegions)
            .build();
    }

    @Nested
    @DisplayName("无可用 Variant")
    class NoSaleableVariants {

        @Test
        @DisplayName("Variant 策略为空时，返回全 0 价格")
        void should_return_zero_prices_when_no_variant_policies() {
            when(saleModelVariantPolicyRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(Collections.emptyList());

            SaleModelPriceResult result = saleModelAppService.getSaleModelPrices(SALE_MODEL_CODE, null);

            assertNotNull(result);
            assertEquals(BigDecimal.ZERO, result.getStartingPrice());
            assertEquals(BigDecimal.ZERO, result.getEarnestMoneyPrice());
            assertEquals(BigDecimal.ZERO, result.getDownPaymentPrice());
        }

        @Test
        @DisplayName("所有 Variant 都不可售时，返回全 0 价格")
        void should_return_zero_prices_when_all_variants_inactive() {
            List<SaleModelVariantPolicyPo> policies = Arrays.asList(
                buildVariantPolicy("VAR001", BigDecimal.valueOf(199900), BigDecimal.valueOf(5000), BigDecimal.valueOf(20000), "off_shelf", null),
                buildVariantPolicy("VAR002", BigDecimal.valueOf(229900), BigDecimal.valueOf(5000), BigDecimal.valueOf(20000), "off_shelf", null)
            );
            when(saleModelVariantPolicyRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(policies);

            SaleModelPriceResult result = saleModelAppService.getSaleModelPrices(SALE_MODEL_CODE, null);

            assertNotNull(result);
            assertEquals(BigDecimal.ZERO, result.getStartingPrice());
            assertEquals(BigDecimal.ZERO, result.getEarnestMoneyPrice());
            assertEquals(BigDecimal.ZERO, result.getDownPaymentPrice());
        }

        @Test
        @DisplayName("所有 Variant 的 variantPrice 为 null 时，返回全 0 价格")
        void should_return_zero_prices_when_all_variant_prices_null() {
            List<SaleModelVariantPolicyPo> policies = Arrays.asList(
                buildVariantPolicy("VAR001", null, BigDecimal.valueOf(5000), BigDecimal.valueOf(20000), "active", null),
                buildVariantPolicy("VAR002", null, BigDecimal.valueOf(5000), BigDecimal.valueOf(20000), "active", null)
            );
            when(saleModelVariantPolicyRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(policies);

            SaleModelPriceResult result = saleModelAppService.getSaleModelPrices(SALE_MODEL_CODE, null);

            assertNotNull(result);
            assertEquals(BigDecimal.ZERO, result.getStartingPrice());
            assertEquals(BigDecimal.ZERO, result.getEarnestMoneyPrice());
            assertEquals(BigDecimal.ZERO, result.getDownPaymentPrice());
        }
    }

    @Nested
    @DisplayName("价格计算")
    class PriceCalculation {

        @Test
        @DisplayName("单个 Variant 时，返回该 Variant 的价格")
        void should_return_single_variant_prices() {
            List<SaleModelVariantPolicyPo> policies = Arrays.asList(
                buildVariantPolicy("VAR001", BigDecimal.valueOf(199900), BigDecimal.valueOf(5000), BigDecimal.valueOf(20000), "active", null)
            );
            when(saleModelVariantPolicyRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(policies);

            SaleModelPriceResult result = saleModelAppService.getSaleModelPrices(SALE_MODEL_CODE, null);

            assertNotNull(result);
            assertEquals(BigDecimal.valueOf(199900), result.getStartingPrice());
            assertEquals(BigDecimal.valueOf(5000), result.getEarnestMoneyPrice());
            assertEquals(BigDecimal.valueOf(20000), result.getDownPaymentPrice());
        }

        @Test
        @DisplayName("多个 Variant 时，返回最低价格")
        void should_return_min_prices_from_multiple_variants() {
            List<SaleModelVariantPolicyPo> policies = Arrays.asList(
                buildVariantPolicy("VAR001", BigDecimal.valueOf(199900), BigDecimal.valueOf(5000), BigDecimal.valueOf(20000), "active", null),
                buildVariantPolicy("VAR002", BigDecimal.valueOf(229900), BigDecimal.valueOf(8000), BigDecimal.valueOf(30000), "active", null),
                buildVariantPolicy("VAR003", BigDecimal.valueOf(179900), BigDecimal.valueOf(3000), BigDecimal.valueOf(15000), "active", null)
            );
            when(saleModelVariantPolicyRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(policies);

            SaleModelPriceResult result = saleModelAppService.getSaleModelPrices(SALE_MODEL_CODE, null);

            assertNotNull(result);
            assertEquals(BigDecimal.valueOf(179900), result.getStartingPrice());
            assertEquals(BigDecimal.valueOf(3000), result.getEarnestMoneyPrice());
            assertEquals(BigDecimal.valueOf(15000), result.getDownPaymentPrice());
        }

        @Test
        @DisplayName("部分 Variant 的 earnestMoneyPrice 为 null 时，忽略 null 值")
        void should_ignore_null_earnest_money_price() {
            List<SaleModelVariantPolicyPo> policies = Arrays.asList(
                buildVariantPolicy("VAR001", BigDecimal.valueOf(199900), null, BigDecimal.valueOf(20000), "active", null),
                buildVariantPolicy("VAR002", BigDecimal.valueOf(229900), BigDecimal.valueOf(8000), BigDecimal.valueOf(30000), "active", null)
            );
            when(saleModelVariantPolicyRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(policies);

            SaleModelPriceResult result = saleModelAppService.getSaleModelPrices(SALE_MODEL_CODE, null);

            assertNotNull(result);
            assertEquals(BigDecimal.valueOf(199900), result.getStartingPrice());
            assertEquals(BigDecimal.valueOf(8000), result.getEarnestMoneyPrice());
            assertEquals(BigDecimal.valueOf(20000), result.getDownPaymentPrice());
        }

        @Test
        @DisplayName("部分 Variant 的 downPaymentPrice 为 null 时，忽略 null 值")
        void should_ignore_null_down_payment_price() {
            List<SaleModelVariantPolicyPo> policies = Arrays.asList(
                buildVariantPolicy("VAR001", BigDecimal.valueOf(199900), BigDecimal.valueOf(5000), null, "active", null),
                buildVariantPolicy("VAR002", BigDecimal.valueOf(229900), BigDecimal.valueOf(8000), BigDecimal.valueOf(30000), "active", null)
            );
            when(saleModelVariantPolicyRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(policies);

            SaleModelPriceResult result = saleModelAppService.getSaleModelPrices(SALE_MODEL_CODE, null);

            assertNotNull(result);
            assertEquals(BigDecimal.valueOf(199900), result.getStartingPrice());
            assertEquals(BigDecimal.valueOf(5000), result.getEarnestMoneyPrice());
            assertEquals(BigDecimal.valueOf(30000), result.getDownPaymentPrice());
        }
    }

    @Nested
    @DisplayName("区域过滤")
    class RegionFilter {

        @Test
        @DisplayName("指定区域时，只返回该区域可售的 Variant 价格")
        void should_filter_by_region_when_specified() {
            List<SaleModelVariantPolicyPo> policies = Arrays.asList(
                buildVariantPolicy("VAR001", BigDecimal.valueOf(199900), BigDecimal.valueOf(5000), BigDecimal.valueOf(20000), "active", "[\"CN\",\"US\"]"),
                buildVariantPolicy("VAR002", BigDecimal.valueOf(179900), BigDecimal.valueOf(3000), BigDecimal.valueOf(15000), "active", "[\"US\"]")
            );
            when(saleModelVariantPolicyRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(policies);

            SaleModelPriceResult result = saleModelAppService.getSaleModelPrices(SALE_MODEL_CODE, "CN");

            assertNotNull(result);
            assertEquals(BigDecimal.valueOf(199900), result.getStartingPrice());
            assertEquals(BigDecimal.valueOf(5000), result.getEarnestMoneyPrice());
            assertEquals(BigDecimal.valueOf(20000), result.getDownPaymentPrice());
        }

        @Test
        @DisplayName("availableRegions 为空时，全国可售")
        void should_include_variant_when_available_regions_empty() {
            List<SaleModelVariantPolicyPo> policies = Arrays.asList(
                buildVariantPolicy("VAR001", BigDecimal.valueOf(199900), BigDecimal.valueOf(5000), BigDecimal.valueOf(20000), "active", null),
                buildVariantPolicy("VAR002", BigDecimal.valueOf(179900), BigDecimal.valueOf(3000), BigDecimal.valueOf(15000), "active", "")
            );
            when(saleModelVariantPolicyRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(policies);

            SaleModelPriceResult result = saleModelAppService.getSaleModelPrices(SALE_MODEL_CODE, "CN");

            assertNotNull(result);
            assertEquals(BigDecimal.valueOf(179900), result.getStartingPrice());
            assertEquals(BigDecimal.valueOf(3000), result.getEarnestMoneyPrice());
            assertEquals(BigDecimal.valueOf(15000), result.getDownPaymentPrice());
        }

        @Test
        @DisplayName("regionCode 为空时，不过滤区域")
        void should_not_filter_when_region_code_is_null() {
            List<SaleModelVariantPolicyPo> policies = Arrays.asList(
                buildVariantPolicy("VAR001", BigDecimal.valueOf(199900), BigDecimal.valueOf(5000), BigDecimal.valueOf(20000), "active", "[\"CN\"]"),
                buildVariantPolicy("VAR002", BigDecimal.valueOf(179900), BigDecimal.valueOf(3000), BigDecimal.valueOf(15000), "active", "[\"US\"]")
            );
            when(saleModelVariantPolicyRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(policies);

            SaleModelPriceResult result = saleModelAppService.getSaleModelPrices(SALE_MODEL_CODE, null);

            assertNotNull(result);
            assertEquals(BigDecimal.valueOf(179900), result.getStartingPrice());
            assertEquals(BigDecimal.valueOf(3000), result.getEarnestMoneyPrice());
            assertEquals(BigDecimal.valueOf(15000), result.getDownPaymentPrice());
        }
    }
}
