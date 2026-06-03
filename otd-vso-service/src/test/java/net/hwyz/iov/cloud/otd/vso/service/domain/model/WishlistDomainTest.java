package net.hwyz.iov.cloud.otd.vso.service.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Wishlist 领域模型单元测试")
class WishlistDomainTest {

    private static final String USER_ID = "user_001";
    private static final String SALE_MODEL_CODE = "SM_001";
    private static final String MODEL_CODE = "MODEL_001";
    private static final String VARIANT_CODE = "VARIANT_001";
    private static final String CONFIG_CODE = "CONFIG_001";
    private static final List<String> OPTION_CODES = Arrays.asList("OPT_001", "OPT_002");

    @Nested
    @DisplayName("create 方法")
    class CreateTest {

        @Test
        @DisplayName("应正确设置所有字段")
        void should_set_all_fields() {
            Wishlist wishlist = Wishlist.create(USER_ID, SALE_MODEL_CODE, MODEL_CODE,
                    VARIANT_CODE, CONFIG_CODE, OPTION_CODES);

            assertNotNull(wishlist.getId());
            assertEquals(USER_ID, wishlist.getUserId());
            assertEquals(SALE_MODEL_CODE, wishlist.getSaleModelCode());
            assertEquals(MODEL_CODE, wishlist.getModelCode());
            assertEquals(VARIANT_CODE, wishlist.getVariantCode());
            assertEquals(CONFIG_CODE, wishlist.getConfigurationCode());
            assertEquals(OPTION_CODES, wishlist.getOptionCodes());
            assertNotNull(wishlist.getOptionCodesHash());
            assertFalse(wishlist.getOptionCodesHash().isEmpty());
            assertEquals(WishlistStatus.ACTIVE, wishlist.getStatus());
            assertNull(wishlist.getInvalidReason());
            assertNotNull(wishlist.getCreateTime());
            assertNotNull(wishlist.getModifyTime());
        }

        @Test
        @DisplayName("optionCodes 为 null 时应初始化为空列表")
        void should_init_empty_list_when_option_codes_null() {
            Wishlist wishlist = Wishlist.create(USER_ID, SALE_MODEL_CODE, MODEL_CODE,
                    VARIANT_CODE, CONFIG_CODE, null);

            assertNotNull(wishlist.getOptionCodes());
            assertTrue(wishlist.getOptionCodes().isEmpty());
        }

        @Test
        @DisplayName("不同调用应生成不同 ID")
        void should_generate_different_ids() {
            Wishlist w1 = Wishlist.create(USER_ID, SALE_MODEL_CODE, MODEL_CODE,
                    VARIANT_CODE, CONFIG_CODE, OPTION_CODES);
            Wishlist w2 = Wishlist.create(USER_ID, SALE_MODEL_CODE, MODEL_CODE,
                    VARIANT_CODE, CONFIG_CODE, OPTION_CODES);

            assertNotEquals(w1.getId(), w2.getId());
        }

        @Test
        @DisplayName("optionCodesHash 应基于排序后的 optionCodes 计算")
        void should_calculate_hash_from_sorted_option_codes() {
            List<String> unsorted = Arrays.asList("OPT_002", "OPT_001");
            List<String> sorted = Arrays.asList("OPT_001", "OPT_002");

            Wishlist w1 = Wishlist.create(USER_ID, SALE_MODEL_CODE, MODEL_CODE,
                    VARIANT_CODE, CONFIG_CODE, unsorted);
            Wishlist w2 = Wishlist.create(USER_ID, SALE_MODEL_CODE, MODEL_CODE,
                    VARIANT_CODE, CONFIG_CODE, sorted);

            assertEquals(w1.getOptionCodesHash(), w2.getOptionCodesHash());
        }
    }

    @Nested
    @DisplayName("modify 方法")
    class ModifyTest {

        @Test
        @DisplayName("应更新 modelCode/variantCode/configurationCode/optionCodes")
        void should_update_all_fields() {
            Wishlist wishlist = Wishlist.create(USER_ID, SALE_MODEL_CODE, MODEL_CODE,
                    VARIANT_CODE, CONFIG_CODE, OPTION_CODES);

            String newModel = "MODEL_002";
            String newVariant = "VARIANT_002";
            String newConfig = "CONFIG_002";
            List<String> newOptions = Arrays.asList("OPT_003");

            wishlist.modify(newModel, newVariant, newConfig, newOptions);

            assertEquals(newModel, wishlist.getModelCode());
            assertEquals(newVariant, wishlist.getVariantCode());
            assertEquals(newConfig, wishlist.getConfigurationCode());
            assertEquals(newOptions, wishlist.getOptionCodes());
        }

        @Test
        @DisplayName("修改后应清除 invalidReason")
        void should_clear_invalid_reason() {
            Wishlist wishlist = Wishlist.create(USER_ID, SALE_MODEL_CODE, MODEL_CODE,
                    VARIANT_CODE, CONFIG_CODE, OPTION_CODES);
            wishlist.markInvalid("VARIANT_OFF_SHELF");

            wishlist.modify(MODEL_CODE, VARIANT_CODE, CONFIG_CODE, OPTION_CODES);

            assertNull(wishlist.getInvalidReason());
        }

        @Test
        @DisplayName("修改后应更新 modifyTime")
        void should_update_modify_time() {
            Wishlist wishlist = Wishlist.create(USER_ID, SALE_MODEL_CODE, MODEL_CODE,
                    VARIANT_CODE, CONFIG_CODE, OPTION_CODES);
            var oldTime = wishlist.getModifyTime();

            wishlist.modify(MODEL_CODE, VARIANT_CODE, CONFIG_CODE, OPTION_CODES);

            assertTrue(wishlist.getModifyTime().compareTo(oldTime) >= 0);
        }
    }

    @Nested
    @DisplayName("delete 方法")
    class DeleteTest {

        @Test
        @DisplayName("应将状态设为 DELETED")
        void should_set_status_to_deleted() {
            Wishlist wishlist = Wishlist.create(USER_ID, SALE_MODEL_CODE, MODEL_CODE,
                    VARIANT_CODE, CONFIG_CODE, OPTION_CODES);

            wishlist.delete();

            assertEquals(WishlistStatus.DELETED, wishlist.getStatus());
        }
    }

    @Nested
    @DisplayName("markInvalid / clearInvalid 方法")
    class InvalidReasonTest {

        @Test
        @DisplayName("markInvalid 应设置 invalidReason")
        void should_set_invalid_reason() {
            Wishlist wishlist = Wishlist.create(USER_ID, SALE_MODEL_CODE, MODEL_CODE,
                    VARIANT_CODE, CONFIG_CODE, OPTION_CODES);

            wishlist.markInvalid("MODEL_OFF_SHELF");

            assertEquals("MODEL_OFF_SHELF", wishlist.getInvalidReason());
        }

        @Test
        @DisplayName("clearInvalid 应清除 invalidReason")
        void should_clear_invalid_reason() {
            Wishlist wishlist = Wishlist.create(USER_ID, SALE_MODEL_CODE, MODEL_CODE,
                    VARIANT_CODE, CONFIG_CODE, OPTION_CODES);
            wishlist.markInvalid("MODEL_OFF_SHELF");

            wishlist.clearInvalid();

            assertNull(wishlist.getInvalidReason());
        }

        @Test
        @DisplayName("markInvalid 应更新 modifyTime")
        void should_update_modify_time_on_mark() {
            Wishlist wishlist = Wishlist.create(USER_ID, SALE_MODEL_CODE, MODEL_CODE,
                    VARIANT_CODE, CONFIG_CODE, OPTION_CODES);
            var oldTime = wishlist.getModifyTime();

            wishlist.markInvalid("VARIANT_OFF_SHELF");

            assertTrue(wishlist.getModifyTime().compareTo(oldTime) >= 0);
        }

        @Test
        @DisplayName("clearInvalid 应更新 modifyTime")
        void should_update_modify_time_on_clear() {
            Wishlist wishlist = Wishlist.create(USER_ID, SALE_MODEL_CODE, MODEL_CODE,
                    VARIANT_CODE, CONFIG_CODE, OPTION_CODES);
            wishlist.markInvalid("VARIANT_OFF_SHELF");
            var markedTime = wishlist.getModifyTime();

            wishlist.clearInvalid();

            assertTrue(wishlist.getModifyTime().compareTo(markedTime) >= 0);
        }
    }
}
