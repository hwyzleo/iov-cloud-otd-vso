package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.converter;

import net.hwyz.iov.cloud.otd.vso.service.domain.model.Wishlist;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.WishlistStatus;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.WishlistPo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("WishlistPoConverter 单元测试")
class WishlistPoConverterTest {

    private static final String WISHLIST_ID = "ws_001";
    private static final String USER_ID = "user_001";
    private static final String SALE_MODEL_CODE = "SM_001";
    private static final String MODEL_CODE = "MODEL_001";
    private static final String VARIANT_CODE = "VARIANT_001";
    private static final String CONFIG_CODE = "CONFIG_001";
    private static final List<String> OPTION_CODES = Arrays.asList("OPT_001", "OPT_002");
    private static final String OPTION_CODES_HASH = "abc123";

    private Wishlist buildDomain() {
        return Wishlist.create(USER_ID, SALE_MODEL_CODE, MODEL_CODE,
                VARIANT_CODE, CONFIG_CODE, OPTION_CODES);
    }

    private WishlistPo buildPo() {
        return WishlistPo.builder()
                .id(100L)
                .wishlistId(WISHLIST_ID)
                .userId(USER_ID)
                .saleModelCode(SALE_MODEL_CODE)
                .modelCode(MODEL_CODE)
                .variantCode(VARIANT_CODE)
                .configurationCode(CONFIG_CODE)
                .optionCodes("[\"OPT_001\",\"OPT_002\"]")
                .optionCodesHash(OPTION_CODES_HASH)
                .wishlistName("测试心愿单")
                .status("ACTIVE")
                .build();
    }

    // ========== toPo ==========

    @Nested
    @DisplayName("toPo 方法（Domain → PO）")
    class ToPoTest {

        @Test
        @DisplayName("应正确映射所有业务字段")
        void should_map_all_business_fields() {
            Wishlist domain = buildDomain();
            WishlistPo po = WishlistPoConverter.INSTANCE.toPo(domain);

            assertEquals(USER_ID, po.getUserId());
            assertEquals(SALE_MODEL_CODE, po.getSaleModelCode());
            assertEquals(MODEL_CODE, po.getModelCode());
            assertEquals(VARIANT_CODE, po.getVariantCode());
            assertEquals(CONFIG_CODE, po.getConfigurationCode());
            assertNotNull(po.getOptionCodes());
            assertNotNull(po.getOptionCodesHash());
            assertEquals(WishlistStatus.ACTIVE.name(), po.getStatus());
        }

        @Test
        @DisplayName("应将 domain.id 映射到 po.wishlistId")
        void should_map_domain_id_to_po_wishlistId() {
            Wishlist domain = buildDomain();
            WishlistPo po = WishlistPoConverter.INSTANCE.toPo(domain);

            assertEquals(domain.getId(), po.getWishlistId());
        }

        @Test
        @DisplayName("po.id 应被忽略（由数据库自增生成）")
        void should_ignore_po_id() {
            Wishlist domain = buildDomain();
            WishlistPo po = WishlistPoConverter.INSTANCE.toPo(domain);

            assertNull(po.getId());
        }

        @Test
        @DisplayName("optionCodes 列表应序列化为 JSON 字符串")
        void should_serialize_option_codes_to_json() {
            Wishlist domain = buildDomain();
            WishlistPo po = WishlistPoConverter.INSTANCE.toPo(domain);

            assertNotNull(po.getOptionCodes());
            assertTrue(po.getOptionCodes().contains("OPT_001"));
            assertTrue(po.getOptionCodes().contains("OPT_002"));
        }

        @Test
        @DisplayName("optionCodes 为空列表时应序列化为空 JSON 数组")
        void should_serialize_empty_option_codes_to_empty_json_array() {
            Wishlist domain = Wishlist.create(USER_ID, SALE_MODEL_CODE, MODEL_CODE,
                    VARIANT_CODE, CONFIG_CODE, Collections.emptyList());
            WishlistPo po = WishlistPoConverter.INSTANCE.toPo(domain);

            assertEquals("[]", po.getOptionCodes());
        }

        @Test
        @DisplayName("DB NOT NULL 字段均不应为 null（防止 sale_model 事件重演）")
        void should_ensure_all_not_null_fields_are_populated() {
            Wishlist domain = buildDomain();
            WishlistPo po = WishlistPoConverter.INSTANCE.toPo(domain);

            assertNotNull(po.getWishlistId(), "wishlistId 不应为 null");
            assertNotNull(po.getUserId(), "userId 不应为 null");
            assertNotNull(po.getSaleModelCode(), "saleModelCode 不应为 null");
            assertNotNull(po.getStatus(), "status 不应为 null");
            assertNotNull(po.getOptionCodes(), "optionCodes 不应为 null（DB NOT NULL）");
            assertNotNull(po.getOptionCodesHash(), "optionCodesHash 不应为 null（DB NOT NULL）");
        }
    }

    // ========== toDomain ==========

    @Nested
    @DisplayName("toDomain 方法（PO → Domain）")
    class ToDomainTest {

        @Test
        @DisplayName("应正确映射所有业务字段")
        void should_map_all_business_fields() {
            WishlistPo po = buildPo();
            Wishlist domain = WishlistPoConverter.INSTANCE.toDomain(po);

            assertEquals(USER_ID, domain.getUserId());
            assertEquals(SALE_MODEL_CODE, domain.getSaleModelCode());
            assertEquals(MODEL_CODE, domain.getModelCode());
            assertEquals(VARIANT_CODE, domain.getVariantCode());
            assertEquals(CONFIG_CODE, domain.getConfigurationCode());
            assertNotNull(domain.getOptionCodes());
            assertEquals(WishlistStatus.ACTIVE, domain.getStatus());
        }

        @Test
        @DisplayName("应将 po.wishlistId 映射到 domain.id")
        void should_map_po_wishlistId_to_domain_id() {
            WishlistPo po = buildPo();
            Wishlist domain = WishlistPoConverter.INSTANCE.toDomain(po);

            assertEquals(WISHLIST_ID, domain.getId());
        }

        @Test
        @DisplayName("optionCodes JSON 字符串应反序列化为列表")
        void should_deserialize_option_codes_json_to_list() {
            WishlistPo po = buildPo();
            Wishlist domain = WishlistPoConverter.INSTANCE.toDomain(po);

            assertNotNull(domain.getOptionCodes());
            assertEquals(2, domain.getOptionCodes().size());
            assertTrue(domain.getOptionCodes().contains("OPT_001"));
            assertTrue(domain.getOptionCodes().contains("OPT_002"));
        }

        @Test
        @DisplayName("optionCodes 为 null 或空时应返回空列表")
        void should_return_empty_list_when_option_codes_null_or_empty() {
            WishlistPo po = buildPo();
            po.setOptionCodes(null);
            Wishlist domain = WishlistPoConverter.INSTANCE.toDomain(po);

            assertNotNull(domain.getOptionCodes());
            assertTrue(domain.getOptionCodes().isEmpty());
        }
    }

    // ========== toDomainList ==========

    @Nested
    @DisplayName("toDomainList 方法")
    class ToDomainListTest {

        @Test
        @DisplayName("应正确转换列表")
        void should_convert_list_correctly() {
            WishlistPo po1 = buildPo();
            WishlistPo po2 = buildPo();
            po2.setWishlistId("ws_002");

            List<Wishlist> domains = WishlistPoConverter.INSTANCE.toDomainList(List.of(po1, po2));

            assertEquals(2, domains.size());
            assertEquals(WISHLIST_ID, domains.get(0).getId());
            assertEquals("ws_002", domains.get(1).getId());
        }

        @Test
        @DisplayName("空列表应返回空列表")
        void should_return_empty_list_for_empty_input() {
            List<Wishlist> domains = WishlistPoConverter.INSTANCE.toDomainList(Collections.emptyList());

            assertNotNull(domains);
            assertTrue(domains.isEmpty());
        }
    }
}
