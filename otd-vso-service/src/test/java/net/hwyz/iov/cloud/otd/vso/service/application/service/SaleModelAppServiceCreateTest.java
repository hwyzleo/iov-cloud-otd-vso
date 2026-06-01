package net.hwyz.iov.cloud.otd.vso.service.application.service;

import net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo.SaleModelCreateDto;
import net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo.SaleModelUpdateDto;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.SaleModelVariantLockedException;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.SaleModelRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.WishlistRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelPo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SaleModelAppService - 创建/修改销售车型测试")
class SaleModelAppServiceCreateTest {

    @Mock
    private SaleModelRepository saleModelRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private WishlistRepository wishlistRepository;

    @InjectMocks
    private SaleModelAppService saleModelAppService;

    private static final String USER_ID = "test_user";
    private static final String SALE_MODEL_CODE = "TEST_MODEL";

    private SaleModelCreateDto createDto(String code, String modelName, String carlineCode) {
        SaleModelCreateDto dto = new SaleModelCreateDto();
        dto.setSaleModelCode(code);
        dto.setModelName(modelName);
        dto.setCarlineCode(carlineCode);
        return dto;
    }

    private SaleModelUpdateDto updateDto(Long id, String code, String modelName, String carlineCode) {
        SaleModelUpdateDto dto = new SaleModelUpdateDto();
        dto.setId(id);
        dto.setSaleModelCode(code);
        dto.setModelName(modelName);
        dto.setCarlineCode(carlineCode);
        return dto;
    }

    @Nested
    @DisplayName("createSaleModel 方法")
    class CreateSaleModelTest {

        @Test
        @DisplayName("创建销售车型成功")
        void should_create_sale_model_successfully() {
            SaleModelCreateDto dto = createDto(SALE_MODEL_CODE, "测试车型", "CARLINE_001");

            when(saleModelRepository.existsBySaleModelCodeExcludeId(SALE_MODEL_CODE, null))
                .thenReturn(false);
            when(saleModelRepository.existsByCarlineCode("CARLINE_001"))
                .thenReturn(false);
            when(saleModelRepository.insert(any(SaleModelPo.class)))
                .thenAnswer(invocation -> {
                    SaleModelPo po = invocation.getArgument(0);
                    po.setId(1L);
                    return 1;
                });

            Long id = saleModelAppService.createSaleModel(dto, USER_ID);

            assertNotNull(id);
            assertEquals(1L, id);

            ArgumentCaptor<SaleModelPo> captor = ArgumentCaptor.forClass(SaleModelPo.class);
            verify(saleModelRepository).insert(captor.capture());
            SaleModelPo savedPo = captor.getValue();
            assertEquals(SALE_MODEL_CODE, savedPo.getSaleModelCode());
            assertEquals(USER_ID, savedPo.getCreateBy());
            assertFalse(savedPo.getEarnestMoney());
            assertFalse(savedPo.getDownPayment());
            assertTrue(savedPo.getEnable());
            assertEquals(0, savedPo.getSort());
        }

        @Test
        @DisplayName("销售代码已存在时应抛出异常")
        void should_throw_when_sale_model_code_exists() {
            SaleModelCreateDto dto = createDto(SALE_MODEL_CODE, "测试车型", "CARLINE_001");

            when(saleModelRepository.existsBySaleModelCodeExcludeId(SALE_MODEL_CODE, null))
                .thenReturn(true);

            assertThrows(IllegalArgumentException.class, () -> 
                saleModelAppService.createSaleModel(dto, USER_ID));
        }

        @Test
        @DisplayName("CarlineCode 已存在时应抛出异常")
        void should_throw_when_carline_code_exists() {
            SaleModelCreateDto dto = createDto(SALE_MODEL_CODE, "测试车型", "CARLINE_001");

            when(saleModelRepository.existsBySaleModelCodeExcludeId(SALE_MODEL_CODE, null))
                .thenReturn(false);
            when(saleModelRepository.existsByCarlineCode("CARLINE_001"))
                .thenReturn(true);

            assertThrows(IllegalArgumentException.class, () -> 
                saleModelAppService.createSaleModel(dto, USER_ID));
        }

    }

    @Nested
    @DisplayName("modifySaleModel 方法")
    class ModifySaleModelTest {

        @Test
        @DisplayName("修改销售车型成功")
        void should_modify_sale_model_successfully() {
            SaleModelUpdateDto dto = updateDto(1L, SALE_MODEL_CODE, "更新后的车型", null);

            when(saleModelRepository.existsBySaleModelCodeExcludeId(SALE_MODEL_CODE, 1L))
                .thenReturn(false);
            when(saleModelRepository.update(any(SaleModelPo.class)))
                .thenReturn(1);

            assertDoesNotThrow(() -> saleModelAppService.modifySaleModel(dto, USER_ID));
        }

        @Test
        @DisplayName("修改 CarlineCode 时，有活跃订单应抛出异常")
        void should_throw_when_modify_carline_with_active_orders() {
            SaleModelUpdateDto dto = updateDto(1L, SALE_MODEL_CODE, "更新后的车型", "CARLINE_002");

            SaleModelPo existingPo = SaleModelPo.builder()
                .id(1L)
                .saleModelCode(SALE_MODEL_CODE)
                .carlineCode("CARLINE_001")
                .build();

            when(saleModelRepository.existsBySaleModelCodeExcludeId(SALE_MODEL_CODE, 1L))
                .thenReturn(false);
            when(saleModelRepository.findById(1L))
                .thenReturn(Optional.of(existingPo));
            when(orderRepository.existsActiveOrdersBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(true);

            assertThrows(SaleModelVariantLockedException.class, () -> 
                saleModelAppService.modifySaleModel(dto, USER_ID));
        }

        @Test
        @DisplayName("修改 CarlineCode 时，有活跃心愿单应抛出异常")
        void should_throw_when_modify_carline_with_active_wishlists() {
            SaleModelUpdateDto dto = updateDto(1L, SALE_MODEL_CODE, "更新后的车型", "CARLINE_002");

            SaleModelPo existingPo = SaleModelPo.builder()
                .id(1L)
                .saleModelCode(SALE_MODEL_CODE)
                .carlineCode("CARLINE_001")
                .build();

            when(saleModelRepository.existsBySaleModelCodeExcludeId(SALE_MODEL_CODE, 1L))
                .thenReturn(false);
            when(saleModelRepository.findById(1L))
                .thenReturn(Optional.of(existingPo));
            when(orderRepository.existsActiveOrdersBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(false);
            when(wishlistRepository.existsActiveBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(true);

            assertThrows(SaleModelVariantLockedException.class, () -> 
                saleModelAppService.modifySaleModel(dto, USER_ID));
        }

        @Test
        @DisplayName("修改 CarlineCode 时，新 CarlineCode 已被其他车型使用应抛出异常")
        void should_throw_when_new_carline_code_used_by_other() {
            SaleModelUpdateDto dto = updateDto(1L, SALE_MODEL_CODE, "更新后的车型", "CARLINE_002");

            SaleModelPo existingPo = SaleModelPo.builder()
                .id(1L)
                .saleModelCode(SALE_MODEL_CODE)
                .carlineCode("CARLINE_001")
                .build();

            when(saleModelRepository.existsBySaleModelCodeExcludeId(SALE_MODEL_CODE, 1L))
                .thenReturn(false);
            when(saleModelRepository.findById(1L))
                .thenReturn(Optional.of(existingPo));
            when(orderRepository.existsActiveOrdersBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(false);
            when(wishlistRepository.existsActiveBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(false);
            when(saleModelRepository.existsByCarlineCodeExcludeId("CARLINE_002", 1L))
                .thenReturn(true);

            assertThrows(IllegalArgumentException.class, () -> 
                saleModelAppService.modifySaleModel(dto, USER_ID));
        }

        @Test
        @DisplayName("CarlineCode 未变化时不校验锁定")
        void should_skip_lock_check_when_carline_not_changed() {
            SaleModelUpdateDto dto = updateDto(1L, SALE_MODEL_CODE, "更新后的车型", "CARLINE_001");

            SaleModelPo existingPo = SaleModelPo.builder()
                .id(1L)
                .saleModelCode(SALE_MODEL_CODE)
                .carlineCode("CARLINE_001")
                .build();

            when(saleModelRepository.existsBySaleModelCodeExcludeId(SALE_MODEL_CODE, 1L))
                .thenReturn(false);
            when(saleModelRepository.findById(1L))
                .thenReturn(Optional.of(existingPo));
            when(saleModelRepository.update(any(SaleModelPo.class)))
                .thenReturn(1);

            assertDoesNotThrow(() -> saleModelAppService.modifySaleModel(dto, USER_ID));
            verify(orderRepository, never()).existsActiveOrdersBySaleModelCode(any());
        }
    }
}
