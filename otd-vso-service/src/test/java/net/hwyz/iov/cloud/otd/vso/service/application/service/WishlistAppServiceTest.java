package net.hwyz.iov.cloud.otd.vso.service.application.service;

import net.hwyz.iov.cloud.edd.mdm.api.service.ConfigurationService;
import net.hwyz.iov.cloud.edd.mdm.api.vo.request.ConfigurationByVariantAndOptionCodesRequest;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.CreateWishlistCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.ModifyWishlistCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.WishlistDetailResult;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.WishlistListResult;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.ConfigurationNotMatchedException;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.DuplicateWishlistException;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.WishlistLimitExceededException;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Wishlist;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.WishlistInvalidReason;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.WishlistStatus;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.SaleModelRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.WishlistRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.service.SalesPolicyService;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelPo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WishlistAppService 单元测试")
class WishlistAppServiceTest {

    @Mock
    private WishlistRepository wishlistRepository;

    @Mock
    private SaleModelRepository saleModelRepository;

    @Mock
    private SalesPolicyService salesPolicyService;

    @Mock
    private ConfigurationService configurationService;

    @InjectMocks
    private WishlistAppService wishlistAppService;

    private static final String ACCOUNT_ID = "user_001";
    private static final String SALE_MODEL_CODE = "SM_001";
    private static final String MODEL_CODE = "MODEL_001";
    private static final String VARIANT_CODE = "VARIANT_001";
    private static final String CONFIG_CODE = "CONFIG_001";
    private static final List<String> OPTION_CODES = Arrays.asList("OPT_001", "OPT_002");

    private SaleModelPo buildActiveSaleModel() {
        return SaleModelPo.builder()
                .id(1L)
                .saleModelCode(SALE_MODEL_CODE)
                .listingStatus("active")
                .effectiveFrom(Timestamp.from(Instant.now().minus(30, ChronoUnit.DAYS)))
                .effectiveTo(Timestamp.from(Instant.now().plus(30, ChronoUnit.DAYS)))
                .build();
    }

    private Wishlist buildWishlist(String id) {
        return Wishlist.create(ACCOUNT_ID, SALE_MODEL_CODE, MODEL_CODE,
                VARIANT_CODE, CONFIG_CODE, OPTION_CODES);
    }

    // ========== createWishlist ==========

    @Nested
    @DisplayName("createWishlist 方法")
    class CreateWishlistTest {

        @Test
        @DisplayName("校验通过时应成功创建心愿单")
        void should_create_wishlist_when_validation_passes() {
            when(wishlistRepository.countByUserId(ACCOUNT_ID)).thenReturn(0L);
            when(saleModelRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(Optional.of(buildActiveSaleModel()));
            when(configurationService.resolveConfiguration(any(ConfigurationByVariantAndOptionCodesRequest.class)))
                .thenReturn(CONFIG_CODE);

            String wishlistId = wishlistAppService.createWishlist(
                CreateWishlistCmd.builder()
                    .accountId(ACCOUNT_ID)
                    .saleModelCode(SALE_MODEL_CODE)
                    .modelCode(MODEL_CODE)
                    .variantCode(VARIANT_CODE)
                    .optionCodes(OPTION_CODES)
                    .build());

            assertNotNull(wishlistId);
            verify(wishlistRepository).save(any(Wishlist.class));
        }

        @Test
        @DisplayName("心愿单数量达上限时应抛出异常")
        void should_throw_when_wishlist_limit_exceeded() {
            when(wishlistRepository.countByUserId(ACCOUNT_ID)).thenReturn(5L);

            assertThrows(WishlistLimitExceededException.class, () ->
                wishlistAppService.createWishlist(
                    CreateWishlistCmd.builder()
                        .accountId(ACCOUNT_ID)
                        .saleModelCode(SALE_MODEL_CODE)
                        .modelCode(MODEL_CODE)
                        .variantCode(VARIANT_CODE)
                        .optionCodes(OPTION_CODES)
                        .build()));
        }

        @Test
        @DisplayName("resolveConfiguration 返回 null 时应抛出异常")
        void should_throw_when_resolve_configuration_returns_null() {
            when(wishlistRepository.countByUserId(ACCOUNT_ID)).thenReturn(0L);
            when(saleModelRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(Optional.of(buildActiveSaleModel()));
            when(configurationService.resolveConfiguration(any(ConfigurationByVariantAndOptionCodesRequest.class)))
                .thenReturn(null);

            assertThrows(ConfigurationNotMatchedException.class, () ->
                wishlistAppService.createWishlist(
                    CreateWishlistCmd.builder()
                        .accountId(ACCOUNT_ID)
                        .saleModelCode(SALE_MODEL_CODE)
                        .modelCode(MODEL_CODE)
                        .variantCode(VARIANT_CODE)
                        .optionCodes(OPTION_CODES)
                        .build()));
        }

        @Test
        @DisplayName("存在重复心愿单时应抛出异常")
        void should_throw_when_duplicate_wishlist_exists() {
            when(wishlistRepository.countByUserId(ACCOUNT_ID)).thenReturn(0L);
            when(saleModelRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(Optional.of(buildActiveSaleModel()));
            when(configurationService.resolveConfiguration(any(ConfigurationByVariantAndOptionCodesRequest.class)))
                .thenReturn(CONFIG_CODE);
            when(wishlistRepository.existsByUniqueKey(eq(ACCOUNT_ID), eq(SALE_MODEL_CODE), eq(MODEL_CODE),
                    eq(VARIANT_CODE), eq(CONFIG_CODE), anyString())).thenReturn(true);

            assertThrows(DuplicateWishlistException.class, () ->
                wishlistAppService.createWishlist(
                    CreateWishlistCmd.builder()
                        .accountId(ACCOUNT_ID)
                        .saleModelCode(SALE_MODEL_CODE)
                        .modelCode(MODEL_CODE)
                        .variantCode(VARIANT_CODE)
                        .optionCodes(OPTION_CODES)
                        .build()));
        }
    }

    // ========== modifyWishlist ==========

    @Nested
    @DisplayName("modifyWishlist 方法")
    class ModifyWishlistTest {

        @Test
        @DisplayName("校验通过时应成功修改心愿单")
        void should_modify_wishlist_when_validation_passes() {
            Wishlist existing = buildWishlist("ws_001");
            when(wishlistRepository.findByWishlistIdAndUserId("ws_001", ACCOUNT_ID))
                .thenReturn(Optional.of(existing));
            when(saleModelRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(Optional.of(buildActiveSaleModel()));
            when(configurationService.resolveConfiguration(any(ConfigurationByVariantAndOptionCodesRequest.class)))
                .thenReturn(CONFIG_CODE);

            wishlistAppService.modifyWishlist(
                ModifyWishlistCmd.builder()
                    .accountId(ACCOUNT_ID)
                    .wishlistId("ws_001")
                    .modelCode("MODEL_002")
                    .variantCode("VARIANT_002")
                    .optionCodes(OPTION_CODES)
                    .build());

            verify(wishlistRepository).save(any(Wishlist.class));
        }
    }

    // ========== getWishlistDetail ==========

    @Nested
    @DisplayName("getWishlistDetail 方法（实时校验）")
    class GetWishlistDetailTest {

        @Test
        @DisplayName("心愿单有效时 invalidReason 应为 null")
        void should_return_null_invalid_reason_when_wishlist_is_valid() {
            Wishlist wishlist = buildWishlist("ws_001");
            when(wishlistRepository.findByWishlistIdAndUserId("ws_001", ACCOUNT_ID))
                .thenReturn(Optional.of(wishlist));
            when(saleModelRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(Optional.of(buildActiveSaleModel()));
            when(salesPolicyService.checkModelForSale(SALE_MODEL_CODE, MODEL_CODE))
                .thenReturn(null);
            when(salesPolicyService.checkVariantForSale(SALE_MODEL_CODE, VARIANT_CODE))
                .thenReturn(null);
            when(salesPolicyService.checkConfigurationForSale(SALE_MODEL_CODE, CONFIG_CODE))
                .thenReturn(null);
            when(salesPolicyService.checkOptionsForSale(SALE_MODEL_CODE, OPTION_CODES))
                .thenReturn(null);

            WishlistDetailResult result = wishlistAppService.getWishlistDetail(ACCOUNT_ID, "ws_001");

            assertNull(result.getInvalidReason());
        }

        @Test
        @DisplayName("SaleModel 下架时应标注 SALE_MODEL_OFF_SHELF")
        void should_mark_sale_model_off_shelf_when_listing_status_not_active() {
            Wishlist wishlist = buildWishlist("ws_001");
            when(wishlistRepository.findByWishlistIdAndUserId("ws_001", ACCOUNT_ID))
                .thenReturn(Optional.of(wishlist));

            SaleModelPo offShelfModel = buildActiveSaleModel();
            offShelfModel.setListingStatus("off_shelf");
            when(saleModelRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(Optional.of(offShelfModel));

            WishlistDetailResult result = wishlistAppService.getWishlistDetail(ACCOUNT_ID, "ws_001");

            assertEquals("SALE_MODEL_OFF_SHELF", result.getInvalidReason());
            verify(wishlistRepository).save(any(Wishlist.class));
        }

        @Test
        @DisplayName("Model 不可售时应标注 MODEL_OFF_SHELF")
        void should_mark_model_off_shelf_when_model_not_for_sale() {
            Wishlist wishlist = buildWishlist("ws_001");
            when(wishlistRepository.findByWishlistIdAndUserId("ws_001", ACCOUNT_ID))
                .thenReturn(Optional.of(wishlist));
            when(saleModelRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(Optional.of(buildActiveSaleModel()));
            when(salesPolicyService.checkModelForSale(SALE_MODEL_CODE, MODEL_CODE))
                .thenReturn(WishlistInvalidReason.MODEL_OFF_SHELF);

            WishlistDetailResult result = wishlistAppService.getWishlistDetail(ACCOUNT_ID, "ws_001");

            assertEquals("MODEL_OFF_SHELF", result.getInvalidReason());
            verify(wishlistRepository).save(any(Wishlist.class));
        }

        @Test
        @DisplayName("Variant 不可售时应标注 VARIANT_OFF_SHELF")
        void should_mark_variant_off_shelf_when_variant_not_for_sale() {
            Wishlist wishlist = buildWishlist("ws_001");
            when(wishlistRepository.findByWishlistIdAndUserId("ws_001", ACCOUNT_ID))
                .thenReturn(Optional.of(wishlist));
            when(saleModelRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(Optional.of(buildActiveSaleModel()));
            when(salesPolicyService.checkModelForSale(SALE_MODEL_CODE, MODEL_CODE))
                .thenReturn(null);
            when(salesPolicyService.checkVariantForSale(SALE_MODEL_CODE, VARIANT_CODE))
                .thenReturn(WishlistInvalidReason.VARIANT_OFF_SHELF);

            WishlistDetailResult result = wishlistAppService.getWishlistDetail(ACCOUNT_ID, "ws_001");

            assertEquals("VARIANT_OFF_SHELF", result.getInvalidReason());
            verify(wishlistRepository).save(any(Wishlist.class));
        }

        @Test
        @DisplayName("Option 不可售时应标注 OPTION_OFF_SHELF")
        void should_mark_option_off_shelf_when_option_not_for_sale() {
            Wishlist wishlist = buildWishlist("ws_001");
            when(wishlistRepository.findByWishlistIdAndUserId("ws_001", ACCOUNT_ID))
                .thenReturn(Optional.of(wishlist));
            when(saleModelRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(Optional.of(buildActiveSaleModel()));
            when(salesPolicyService.checkModelForSale(SALE_MODEL_CODE, MODEL_CODE))
                .thenReturn(null);
            when(salesPolicyService.checkVariantForSale(SALE_MODEL_CODE, VARIANT_CODE))
                .thenReturn(null);
            when(salesPolicyService.checkConfigurationForSale(SALE_MODEL_CODE, CONFIG_CODE))
                .thenReturn(null);
            when(salesPolicyService.checkOptionsForSale(SALE_MODEL_CODE, OPTION_CODES))
                .thenReturn(WishlistInvalidReason.OPTION_OFF_SHELF);

            WishlistDetailResult result = wishlistAppService.getWishlistDetail(ACCOUNT_ID, "ws_001");

            assertEquals("OPTION_OFF_SHELF", result.getInvalidReason());
            verify(wishlistRepository).save(any(Wishlist.class));
        }

        @Test
        @DisplayName("之前失效但现在有效时应清除 invalidReason")
        void should_clear_invalid_reason_when_wishlist_becomes_valid() {
            Wishlist wishlist = buildWishlist("ws_001");
            wishlist.markInvalid("VARIANT_OFF_SHELF");

            when(wishlistRepository.findByWishlistIdAndUserId("ws_001", ACCOUNT_ID))
                .thenReturn(Optional.of(wishlist));
            when(saleModelRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(Optional.of(buildActiveSaleModel()));
            when(salesPolicyService.checkModelForSale(SALE_MODEL_CODE, MODEL_CODE))
                .thenReturn(null);
            when(salesPolicyService.checkVariantForSale(SALE_MODEL_CODE, VARIANT_CODE))
                .thenReturn(null);
            when(salesPolicyService.checkConfigurationForSale(SALE_MODEL_CODE, CONFIG_CODE))
                .thenReturn(null);
            when(salesPolicyService.checkOptionsForSale(SALE_MODEL_CODE, OPTION_CODES))
                .thenReturn(null);

            WishlistDetailResult result = wishlistAppService.getWishlistDetail(ACCOUNT_ID, "ws_001");

            assertNull(result.getInvalidReason());
            verify(wishlistRepository).save(any(Wishlist.class));
        }

        @Test
        @DisplayName("失效原因未变化时不应重复保存")
        void should_not_save_when_invalid_reason_unchanged() {
            Wishlist wishlist = buildWishlist("ws_001");
            wishlist.markInvalid("MODEL_OFF_SHELF");

            when(wishlistRepository.findByWishlistIdAndUserId("ws_001", ACCOUNT_ID))
                .thenReturn(Optional.of(wishlist));
            when(saleModelRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(Optional.of(buildActiveSaleModel()));
            when(salesPolicyService.checkModelForSale(SALE_MODEL_CODE, MODEL_CODE))
                .thenReturn(WishlistInvalidReason.MODEL_OFF_SHELF);

            WishlistDetailResult result = wishlistAppService.getWishlistDetail(ACCOUNT_ID, "ws_001");

            assertEquals("MODEL_OFF_SHELF", result.getInvalidReason());
            verify(wishlistRepository, never()).save(any(Wishlist.class));
        }
    }

    // ========== getWishlistList ==========

    @Nested
    @DisplayName("getWishlistList 方法（实时校验）")
    class GetWishlistListTest {

        @Test
        @DisplayName("列表中每个心愿单都应经过实时校验")
        void should_validate_each_wishlist_in_list() {
            Wishlist w1 = buildWishlist("ws_001");
            Wishlist w2 = buildWishlist("ws_002");

            when(wishlistRepository.findByUserId(ACCOUNT_ID))
                .thenReturn(Arrays.asList(w1, w2));
            when(saleModelRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(Optional.of(buildActiveSaleModel()));
            when(salesPolicyService.checkModelForSale(SALE_MODEL_CODE, MODEL_CODE))
                .thenReturn(null);
            when(salesPolicyService.checkVariantForSale(SALE_MODEL_CODE, VARIANT_CODE))
                .thenReturn(null);
            when(salesPolicyService.checkConfigurationForSale(SALE_MODEL_CODE, CONFIG_CODE))
                .thenReturn(null);
            when(salesPolicyService.checkOptionsForSale(SALE_MODEL_CODE, OPTION_CODES))
                .thenReturn(null);

            List<WishlistListResult> results = wishlistAppService.getWishlistList(ACCOUNT_ID);

            assertEquals(2, results.size());
            assertNull(results.get(0).getInvalidReason());
            assertNull(results.get(1).getInvalidReason());
        }

        @Test
        @DisplayName("列表中部分心愿单失效时应分别标注")
        void should_mark_different_invalid_reasons_for_different_wishlists() {
            Wishlist w1 = buildWishlist("ws_001");
            Wishlist w2 = buildWishlist("ws_002");

            when(wishlistRepository.findByUserId(ACCOUNT_ID))
                .thenReturn(Arrays.asList(w1, w2));
            when(saleModelRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(Optional.of(buildActiveSaleModel()));
            when(salesPolicyService.checkModelForSale(SALE_MODEL_CODE, MODEL_CODE))
                .thenReturn(null);
            when(salesPolicyService.checkVariantForSale(SALE_MODEL_CODE, VARIANT_CODE))
                .thenReturn(WishlistInvalidReason.VARIANT_OFF_SHELF);

            List<WishlistListResult> results = wishlistAppService.getWishlistList(ACCOUNT_ID);

            assertEquals(2, results.size());
            assertEquals("VARIANT_OFF_SHELF", results.get(0).getInvalidReason());
            assertEquals("VARIANT_OFF_SHELF", results.get(1).getInvalidReason());
        }
    }
}
