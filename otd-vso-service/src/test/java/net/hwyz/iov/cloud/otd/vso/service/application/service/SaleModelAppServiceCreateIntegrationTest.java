package net.hwyz.iov.cloud.otd.vso.service.application.service;

import net.hwyz.iov.cloud.otd.vso.service.BaseTest;
import net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo.SaleModelCreateDto;
import net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo.SaleModelUpdateDto;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.SaleModelResult;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.SaleModelRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelPo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SaleModelAppService - 创建/修改销售车型集成测试")
@Transactional
class SaleModelAppServiceCreateIntegrationTest extends BaseTest {

    @Autowired
    private SaleModelAppService saleModelAppService;

    @Autowired
    private SaleModelRepository saleModelRepository;

    private static final String USER_ID = "1";

    @Nested
    @DisplayName("createSaleModel 方法")
    class CreateSaleModelTest {

        @Test
        @DisplayName("创建销售车型成功 - 验证数据库写入")
        void should_create_sale_model_and_persist_to_db() {
            String saleModelCode = "TEST_" + System.currentTimeMillis();
            SaleModelCreateDto dto = new SaleModelCreateDto();
            dto.setSaleModelCode(saleModelCode);
            dto.setModelName("测试车型");
            dto.setCarlineCode("CARLINE_" + System.currentTimeMillis());
            dto.setEarnestMoney(true);
            dto.setDownPayment(false);
            dto.setEnable(true);
            dto.setSort(10);
            dto.setListingStatus("active");

            Long id = saleModelAppService.createSaleModel(dto, USER_ID);

            assertNotNull(id);

            // 验证数据库写入
            SaleModelPo savedPo = saleModelRepository.findById(id).orElse(null);
            assertNotNull(savedPo);
            assertEquals(saleModelCode, savedPo.getSaleModelCode());
            assertEquals("测试车型", savedPo.getModelName());
            assertTrue(savedPo.getEarnestMoney());
            assertFalse(savedPo.getDownPayment());
            assertTrue(savedPo.getEnable());
            assertEquals(10, savedPo.getSort());
            assertEquals(USER_ID, savedPo.getCreateBy());
            assertEquals(USER_ID, savedPo.getModifyBy());
            assertNotNull(savedPo.getCreateTime());
            assertNotNull(savedPo.getModifyTime());
        }

        @Test
        @DisplayName("创建销售车型 - 默认值验证")
        void should_set_default_values_when_not_provided() {
            String saleModelCode = "TEST_" + System.currentTimeMillis();
            SaleModelCreateDto dto = new SaleModelCreateDto();
            dto.setSaleModelCode(saleModelCode);
            dto.setModelName("测试车型");
            dto.setCarlineCode("CARLINE_" + System.currentTimeMillis());
            dto.setListingStatus("active");

            Long id = saleModelAppService.createSaleModel(dto, USER_ID);

            SaleModelPo savedPo = saleModelRepository.findById(id).orElse(null);
            assertNotNull(savedPo);
            assertFalse(savedPo.getEarnestMoney());
            assertFalse(savedPo.getDownPayment());
            assertTrue(savedPo.getEnable());
            assertEquals(0, savedPo.getSort());
        }
    }

    @Nested
    @DisplayName("modifySaleModel 方法")
    class ModifySaleModelTest {

        @Test
        @DisplayName("修改销售车型成功 - 验证数据库更新")
        void should_modify_sale_model_and_update_db() {
            // 先创建
            String saleModelCode = "TEST_" + System.currentTimeMillis();
            SaleModelCreateDto createDto = new SaleModelCreateDto();
            createDto.setSaleModelCode(saleModelCode);
            createDto.setModelName("原始车型");
            createDto.setCarlineCode("CARLINE_" + System.currentTimeMillis());
            createDto.setListingStatus("active");

            Long id = saleModelAppService.createSaleModel(createDto, USER_ID);

            // 修改
            SaleModelUpdateDto updateDto = new SaleModelUpdateDto();
            updateDto.setId(id);
            updateDto.setSaleModelCode(saleModelCode);
            updateDto.setModelName("更新后的车型");
            updateDto.setIcon("https://example.com/icon.png");
            updateDto.setMarketingCopy("新的卖点文案");

            saleModelAppService.modifySaleModel(updateDto, "2");

            // 验证数据库更新
            SaleModelPo updatedPo = saleModelRepository.findById(id).orElse(null);
            assertNotNull(updatedPo);
            assertEquals("更新后的车型", updatedPo.getModelName());
            assertEquals("https://example.com/icon.png", updatedPo.getIcon());
            assertEquals("新的卖点文案", updatedPo.getMarketingCopy());
            assertEquals("2", updatedPo.getModifyBy());
        }
    }

    @Nested
    @DisplayName("deleteSaleModelByIds 方法")
    class DeleteSaleModelTest {

        @Test
        @DisplayName("删除销售车型 - 验证物理删除")
        void should_delete_sale_model_from_db() {
            // 先创建
            String saleModelCode = "TEST_" + System.currentTimeMillis();
            SaleModelCreateDto createDto = new SaleModelCreateDto();
            createDto.setSaleModelCode(saleModelCode);
            createDto.setModelName("待删除车型");
            createDto.setCarlineCode("CARLINE_" + System.currentTimeMillis());
            createDto.setListingStatus("active");

            Long id = saleModelAppService.createSaleModel(createDto, USER_ID);
            assertTrue(saleModelRepository.findById(id).isPresent());

            // 删除（物理删除）
            saleModelAppService.deleteSaleModelByIds(new Long[]{id}, USER_ID);

            // 验证物理删除：记录不存在
            assertFalse(saleModelRepository.findById(id).isPresent());
        }
    }

    @Nested
    @DisplayName("getSaleModelById 方法")
    class GetSaleModelTest {

        @Test
        @DisplayName("按 ID 查询销售车型 - 验证数据库读取")
        void should_read_sale_model_from_db() {
            // 先创建
            String saleModelCode = "TEST_" + System.currentTimeMillis();
            SaleModelCreateDto createDto = new SaleModelCreateDto();
            createDto.setSaleModelCode(saleModelCode);
            createDto.setModelName("查询测试车型");
            createDto.setCarlineCode("CARLINE_" + System.currentTimeMillis());
            createDto.setListingStatus("active");

            Long id = saleModelAppService.createSaleModel(createDto, USER_ID);

            // 查询
            SaleModelResult result = saleModelAppService.getSaleModelById(id);

            assertNotNull(result);
            assertEquals(id, result.getId());
            assertEquals(saleModelCode, result.getSaleModelCode());
            assertEquals("查询测试车型", result.getModelName());
        }
    }
}
