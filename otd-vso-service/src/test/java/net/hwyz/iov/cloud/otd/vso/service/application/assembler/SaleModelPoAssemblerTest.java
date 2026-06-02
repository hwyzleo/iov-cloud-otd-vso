package net.hwyz.iov.cloud.otd.vso.service.application.assembler;

import net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo.SaleModelCreateDto;
import net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo.SaleModelUpdateDto;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelPo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SaleModelPoAssembler 单元测试
 * 验证 DTO 到 PO 的转换逻辑，特别是 JSON 字段的序列化
 */
@DisplayName("SaleModelPoAssembler - DTO 转 PO 测试")
class SaleModelPoAssemblerTest {

    private final SaleModelPoAssembler assembler = SaleModelPoAssembler.INSTANCE;

    @Nested
    @DisplayName("toDo 方法 - 创建 DTO 转 PO")
    class ToDoTest {

        @Test
        @DisplayName("images 字段为 null 时，PO 的 images 应为 null")
        void should_set_images_null_when_dto_images_is_null() {
            SaleModelCreateDto dto = new SaleModelCreateDto();
            dto.setImages(null);

            SaleModelPo po = assembler.toDo(dto);

            assertNull(po.getImages());
        }

        @Test
        @DisplayName("images 字段为空列表时，PO 的 images 应为空数组 JSON")
        void should_set_empty_array_json_when_dto_images_is_empty_list() {
            SaleModelCreateDto dto = new SaleModelCreateDto();
            dto.setImages(Collections.emptyList());

            SaleModelPo po = assembler.toDo(dto);

            assertEquals("[]", po.getImages());
        }

        @Test
        @DisplayName("images 字段有值时，PO 的 images 应为有效 JSON 数组")
        void should_set_valid_json_array_when_dto_images_has_values() {
            SaleModelCreateDto dto = new SaleModelCreateDto();
            dto.setImages(Arrays.asList("image1.jpg", "image2.jpg", "image3.jpg"));

            SaleModelPo po = assembler.toDo(dto);

            assertNotNull(po.getImages());
            assertEquals("[\"image1.jpg\",\"image2.jpg\",\"image3.jpg\"]", po.getImages());
        }

        @Test
        @DisplayName("images 字段包含特殊字符时，应正确转义")
        void should_escape_special_characters_in_images() {
            SaleModelCreateDto dto = new SaleModelCreateDto();
            dto.setImages(Arrays.asList("image with space.jpg", "image\"quote.jpg"));

            SaleModelPo po = assembler.toDo(dto);

            assertNotNull(po.getImages());
            assertTrue(po.getImages().contains("image with space.jpg"));
        }

        @Test
        @DisplayName("availableRegions 字段应正确转换为 JSON")
        void should_convert_available_regions_to_json() {
            SaleModelCreateDto dto = new SaleModelCreateDto();
            dto.setAvailableRegions(Arrays.asList("region1", "region2"));

            SaleModelPo po = assembler.toDo(dto);

            assertNotNull(po.getAvailableRegions());
            assertEquals("[\"region1\",\"region2\"]", po.getAvailableRegions());
        }

        @Test
        @DisplayName("channels 字段应正确转换为 JSON")
        void should_convert_channels_to_json() {
            SaleModelCreateDto dto = new SaleModelCreateDto();
            dto.setChannels(Arrays.asList("channel1", "channel2"));

            SaleModelPo po = assembler.toDo(dto);

            assertNotNull(po.getChannels());
            assertEquals("[\"channel1\",\"channel2\"]", po.getChannels());
        }

        @Test
        @DisplayName("所有 JSON 字段为 null 时，PO 的对应字段应为 null")
        void should_set_all_json_fields_null_when_dto_fields_are_null() {
            SaleModelCreateDto dto = new SaleModelCreateDto();
            dto.setImages(null);
            dto.setAvailableRegions(null);
            dto.setChannels(null);

            SaleModelPo po = assembler.toDo(dto);

            assertNull(po.getImages());
            assertNull(po.getAvailableRegions());
            assertNull(po.getChannels());
        }
    }

    @Nested
    @DisplayName("toUpdateDo 方法 - 更新 DTO 转 PO")
    class ToUpdateDoTest {

        @Test
        @DisplayName("images 字段为 null 时，PO 的 images 应为 null")
        void should_set_images_null_when_dto_images_is_null() {
            SaleModelUpdateDto dto = new SaleModelUpdateDto();
            dto.setImages(null);

            SaleModelPo po = assembler.toUpdateDo(dto);

            assertNull(po.getImages());
        }

        @Test
        @DisplayName("images 字段为空列表时，PO 的 images 应为空数组 JSON")
        void should_set_empty_array_json_when_dto_images_is_empty_list() {
            SaleModelUpdateDto dto = new SaleModelUpdateDto();
            dto.setImages(Collections.emptyList());

            SaleModelPo po = assembler.toUpdateDo(dto);

            assertEquals("[]", po.getImages());
        }

        @Test
        @DisplayName("images 字段有值时，PO 的 images 应为有效 JSON 数组")
        void should_set_valid_json_array_when_dto_images_has_values() {
            SaleModelUpdateDto dto = new SaleModelUpdateDto();
            dto.setImages(Arrays.asList("new_image1.jpg", "new_image2.jpg"));

            SaleModelPo po = assembler.toUpdateDo(dto);

            assertNotNull(po.getImages());
            assertEquals("[\"new_image1.jpg\",\"new_image2.jpg\"]", po.getImages());
        }

        @Test
        @DisplayName("images 字段包含中文时，应正确处理")
        void should_handle_chinese_characters_in_images() {
            SaleModelUpdateDto dto = new SaleModelUpdateDto();
            dto.setImages(Arrays.asList("图片1.jpg", "图片2.jpg"));

            SaleModelPo po = assembler.toUpdateDo(dto);

            assertNotNull(po.getImages());
            assertTrue(po.getImages().contains("图片1.jpg"));
            assertTrue(po.getImages().contains("图片2.jpg"));
        }

        @Test
        @DisplayName("images 字段包含 URL 时，应正确处理")
        void should_handle_url_in_images() {
            SaleModelUpdateDto dto = new SaleModelUpdateDto();
            dto.setImages(Arrays.asList(
                "https://example.com/images/car1.jpg",
                "https://example.com/images/car2.jpg"
            ));

            SaleModelPo po = assembler.toUpdateDo(dto);

            assertNotNull(po.getImages());
            assertTrue(po.getImages().contains("https://example.com/images/car1.jpg"));
            assertTrue(po.getImages().contains("https://example.com/images/car2.jpg"));
        }

        @Test
        @DisplayName("availableRegions 字段应正确转换为 JSON")
        void should_convert_available_regions_to_json() {
            SaleModelUpdateDto dto = new SaleModelUpdateDto();
            dto.setAvailableRegions(Arrays.asList("北京", "上海"));

            SaleModelPo po = assembler.toUpdateDo(dto);

            assertNotNull(po.getAvailableRegions());
            assertEquals("[\"北京\",\"上海\"]", po.getAvailableRegions());
        }

        @Test
        @DisplayName("channels 字段应正确转换为 JSON")
        void should_convert_channels_to_json() {
            SaleModelUpdateDto dto = new SaleModelUpdateDto();
            dto.setChannels(Arrays.asList("线上", "线下"));

            SaleModelPo po = assembler.toUpdateDo(dto);

            assertNotNull(po.getChannels());
            assertEquals("[\"线上\",\"线下\"]", po.getChannels());
        }

        @Test
        @DisplayName("单个 images 元素时，应返回单元素数组 JSON")
        void should_return_single_element_array_json_when_one_image() {
            SaleModelUpdateDto dto = new SaleModelUpdateDto();
            dto.setImages(Arrays.asList("single_image.jpg"));

            SaleModelPo po = assembler.toUpdateDo(dto);

            assertNotNull(po.getImages());
            assertEquals("[\"single_image.jpg\"]", po.getImages());
        }

        @Test
        @DisplayName("images 字段包含大量元素时，应正确处理")
        void should_handle_large_number_of_images() {
            SaleModelUpdateDto dto = new SaleModelUpdateDto();
            List<String> images = Arrays.asList(
                "image1.jpg", "image2.jpg", "image3.jpg", "image4.jpg", "image5.jpg",
                "image6.jpg", "image7.jpg", "image8.jpg", "image9.jpg", "image10.jpg"
            );
            dto.setImages(images);

            SaleModelPo po = assembler.toUpdateDo(dto);

            assertNotNull(po.getImages());
            String json = po.getImages();
            assertTrue(json.startsWith("["));
            assertTrue(json.endsWith("]"));
            for (String image : images) {
                assertTrue(json.contains(image));
            }
        }
    }

    @Nested
    @DisplayName("JSON 格式验证")
    class JsonFormatValidationTest {

        @Test
        @DisplayName("生成的 images JSON 应该是有效的 JSON 格式")
        void should_produce_valid_json_for_images() {
            SaleModelUpdateDto dto = new SaleModelUpdateDto();
            dto.setImages(Arrays.asList("test1.jpg", "test2.jpg"));

            SaleModelPo po = assembler.toUpdateDo(dto);

            assertNotNull(po.getImages());
            // 验证是有效的 JSON 数组格式
            assertTrue(po.getImages().startsWith("["));
            assertTrue(po.getImages().endsWith("]"));
            assertTrue(po.getImages().contains("\"test1.jpg\""));
            assertTrue(po.getImages().contains("\"test2.jpg\""));
        }

        @Test
        @DisplayName("生成的 availableRegions JSON 应该是有效的 JSON 格式")
        void should_produce_valid_json_for_available_regions() {
            SaleModelUpdateDto dto = new SaleModelUpdateDto();
            dto.setAvailableRegions(Arrays.asList("region1", "region2"));

            SaleModelPo po = assembler.toUpdateDo(dto);

            assertNotNull(po.getAvailableRegions());
            assertTrue(po.getAvailableRegions().startsWith("["));
            assertTrue(po.getAvailableRegions().endsWith("]"));
            assertTrue(po.getAvailableRegions().contains("\"region1\""));
            assertTrue(po.getAvailableRegions().contains("\"region2\""));
        }

        @Test
        @DisplayName("生成的 channels JSON 应该是有效的 JSON 格式")
        void should_produce_valid_json_for_channels() {
            SaleModelUpdateDto dto = new SaleModelUpdateDto();
            dto.setChannels(Arrays.asList("channel1", "channel2"));

            SaleModelPo po = assembler.toUpdateDo(dto);

            assertNotNull(po.getChannels());
            assertTrue(po.getChannels().startsWith("["));
            assertTrue(po.getChannels().endsWith("]"));
            assertTrue(po.getChannels().contains("\"channel1\""));
            assertTrue(po.getChannels().contains("\"channel2\""));
        }
    }
}
