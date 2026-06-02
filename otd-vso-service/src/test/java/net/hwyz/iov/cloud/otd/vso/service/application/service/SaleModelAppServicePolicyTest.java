package net.hwyz.iov.cloud.otd.vso.service.application.service;

import net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo.ConfigPolicyAvailableVo;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.CreateConfigPolicyCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.CreateOptionPolicyCmd;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.SaleModelConfigPolicyRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.SaleModelOptionPolicyRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.SaleModelRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.service.MdmProjectionService;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.MdmProjectionConfigurationPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelConfigPolicyPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelOptionPolicyPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelPo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SaleModelAppService - 销售策略测试")
class SaleModelAppServicePolicyTest {

    @Mock
    private SaleModelRepository saleModelRepository;

    @Mock
    private SaleModelConfigPolicyRepository configPolicyRepository;

    @Mock
    private SaleModelOptionPolicyRepository optionPolicyRepository;

    @Mock
    private MdmProjectionService mdmProjectionService;

    @InjectMocks
    private SaleModelAppService saleModelAppService;

    private static final String SALE_MODEL_CODE = "TEST_MODEL";
    private static final String CARLINE_CODE = "CARLINE_001";
    private static final String VARIANT_CODE = "VARIANT_001";
    private static final String CONFIG_CODE = "CONFIG_001";
    private static final String OPTION_CODE = "OPT_001";

    @Nested
    @DisplayName("createConfigPolicy 方法")
    class CreateConfigPolicyTest {

        @Test
        @DisplayName("创建 Configuration 白名单成功")
        void should_create_config_policy_successfully() {
            CreateConfigPolicyCmd cmd = CreateConfigPolicyCmd.builder()
                .saleModelCode(SALE_MODEL_CODE)
                .configurationCodes(Arrays.asList("CONFIG_001", "CONFIG_002"))
                .status("active")
                .build();

            when(configPolicyRepository.findBySaleModelCodeAndConfigCode(SALE_MODEL_CODE, "CONFIG_001"))
                .thenReturn(Optional.empty());
            when(configPolicyRepository.findBySaleModelCodeAndConfigCode(SALE_MODEL_CODE, "CONFIG_002"))
                .thenReturn(Optional.empty());

            List<SaleModelConfigPolicyPo> result = saleModelAppService.createConfigPolicy(cmd);

            assertEquals(2, result.size());
            verify(configPolicyRepository, times(2)).save(any(SaleModelConfigPolicyPo.class));
        }

        @Test
        @DisplayName("已存在的 Configuration 应被跳过（reactivate）")
        void should_reactivate_existing_config_policy() {
            CreateConfigPolicyCmd cmd = CreateConfigPolicyCmd.builder()
                .saleModelCode(SALE_MODEL_CODE)
                .configurationCodes(Arrays.asList(CONFIG_CODE))
                .status("active")
                .build();

            SaleModelConfigPolicyPo existingPo = SaleModelConfigPolicyPo.builder()
                .id(1L)
                .saleModelCode(SALE_MODEL_CODE)
                .configurationCode(CONFIG_CODE)
                .status("active")
                .rowValid(false)
                .build();

            when(configPolicyRepository.findBySaleModelCodeAndConfigCode(SALE_MODEL_CODE, CONFIG_CODE))
                .thenReturn(Optional.of(existingPo));

            List<SaleModelConfigPolicyPo> result = saleModelAppService.createConfigPolicy(cmd);

            assertEquals(1, result.size());
            verify(configPolicyRepository).reactivate(1L, "active");
            verify(configPolicyRepository, never()).save(any());
        }

        @Test
        @DisplayName("重复的 Configuration Code 应被去重")
        void should_deduplicate_configuration_codes() {
            CreateConfigPolicyCmd cmd = CreateConfigPolicyCmd.builder()
                .saleModelCode(SALE_MODEL_CODE)
                .configurationCodes(Arrays.asList(CONFIG_CODE, CONFIG_CODE, "CONFIG_002"))
                .status("active")
                .build();

            when(configPolicyRepository.findBySaleModelCodeAndConfigCode(SALE_MODEL_CODE, CONFIG_CODE))
                .thenReturn(Optional.empty());
            when(configPolicyRepository.findBySaleModelCodeAndConfigCode(SALE_MODEL_CODE, "CONFIG_002"))
                .thenReturn(Optional.empty());

            List<SaleModelConfigPolicyPo> result = saleModelAppService.createConfigPolicy(cmd);

            assertEquals(2, result.size());
            verify(configPolicyRepository, times(2)).save(any(SaleModelConfigPolicyPo.class));
        }

        @Test
        @DisplayName("未指定状态时默认使用 active")
        void should_use_default_status_active() {
            CreateConfigPolicyCmd cmd = CreateConfigPolicyCmd.builder()
                .saleModelCode(SALE_MODEL_CODE)
                .configurationCodes(Arrays.asList(CONFIG_CODE))
                .status(null)
                .build();

            when(configPolicyRepository.findBySaleModelCodeAndConfigCode(SALE_MODEL_CODE, CONFIG_CODE))
                .thenReturn(Optional.empty());

            List<SaleModelConfigPolicyPo> result = saleModelAppService.createConfigPolicy(cmd);

            assertEquals(1, result.size());
            assertEquals("active", result.get(0).getStatus());
        }
    }

    @Nested
    @DisplayName("getAvailableConfigPolicies 方法")
    class GetAvailableConfigPoliciesTest {

        @Test
        @DisplayName("按 variantCode 获取可用 Configuration 列表成功")
        void should_get_available_config_policies_by_variant_code() {
            SaleModelPo saleModel = SaleModelPo.builder()
                .saleModelCode(SALE_MODEL_CODE)
                .carlineCode(CARLINE_CODE)
                .build();

            MdmProjectionConfigurationPo config1 = MdmProjectionConfigurationPo.builder()
                .configurationCode("CONFIG_001")
                .variantCode(VARIANT_CODE)
                .optionCodes("[\"OPT_001\",\"OPT_002\"]")
                .guidePrice(BigDecimal.valueOf(100000))
                .build();

            MdmProjectionConfigurationPo config2 = MdmProjectionConfigurationPo.builder()
                .configurationCode("CONFIG_002")
                .variantCode(VARIANT_CODE)
                .optionCodes("[\"OPT_003\"]")
                .guidePrice(BigDecimal.valueOf(120000))
                .build();

            when(saleModelRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(Optional.of(saleModel));
            when(mdmProjectionService.getConfigurationsByVariant(VARIANT_CODE))
                .thenReturn(Arrays.asList(config1, config2));
            when(configPolicyRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(List.of());

            List<ConfigPolicyAvailableVo> result = saleModelAppService.getAvailableConfigPolicies(SALE_MODEL_CODE, VARIANT_CODE);

            assertEquals(2, result.size());
            assertEquals("CONFIG_001", result.get(0).getConfigurationCode());
            assertEquals("CONFIG_002", result.get(1).getConfigurationCode());
            assertFalse(result.get(0).getInWhitelist());
            assertFalse(result.get(1).getInWhitelist());
        }

        @Test
        @DisplayName("白名单中的 Configuration 应标记 inWhitelist=true")
        void should_mark_whitelisted_configurations() {
            SaleModelPo saleModel = SaleModelPo.builder()
                .saleModelCode(SALE_MODEL_CODE)
                .carlineCode(CARLINE_CODE)
                .build();

            MdmProjectionConfigurationPo config1 = MdmProjectionConfigurationPo.builder()
                .configurationCode("CONFIG_001")
                .variantCode(VARIANT_CODE)
                .optionCodes("[\"OPT_001\"]")
                .guidePrice(BigDecimal.valueOf(100000))
                .build();

            MdmProjectionConfigurationPo config2 = MdmProjectionConfigurationPo.builder()
                .configurationCode("CONFIG_002")
                .variantCode(VARIANT_CODE)
                .optionCodes("[\"OPT_002\"]")
                .guidePrice(BigDecimal.valueOf(120000))
                .build();

            SaleModelConfigPolicyPo policy = SaleModelConfigPolicyPo.builder()
                .id(1L)
                .saleModelCode(SALE_MODEL_CODE)
                .variantCode(VARIANT_CODE)
                .configurationCode("CONFIG_001")
                .status("active")
                .build();

            when(saleModelRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(Optional.of(saleModel));
            when(mdmProjectionService.getConfigurationsByVariant(VARIANT_CODE))
                .thenReturn(Arrays.asList(config1, config2));
            when(configPolicyRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(Arrays.asList(policy));

            List<ConfigPolicyAvailableVo> result = saleModelAppService.getAvailableConfigPolicies(SALE_MODEL_CODE, VARIANT_CODE);

            assertEquals(2, result.size());
            assertTrue(result.get(0).getInWhitelist());
            assertEquals("active", result.get(0).getPolicyStatus());
            assertFalse(result.get(1).getInWhitelist());
            assertNull(result.get(1).getPolicyStatus());
        }

        @Test
        @DisplayName("variantCode 下无 Configuration 时返回空列表")
        void should_return_empty_list_when_no_configurations() {
            SaleModelPo saleModel = SaleModelPo.builder()
                .saleModelCode(SALE_MODEL_CODE)
                .carlineCode(CARLINE_CODE)
                .build();

            when(saleModelRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(Optional.of(saleModel));
            when(mdmProjectionService.getConfigurationsByVariant(VARIANT_CODE))
                .thenReturn(List.of());

            List<ConfigPolicyAvailableVo> result = saleModelAppService.getAvailableConfigPolicies(SALE_MODEL_CODE, VARIANT_CODE);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("销售车型不存在时应抛出异常")
        void should_throw_when_sale_model_not_found() {
            when(saleModelRepository.findBySaleModelCode("NON_EXIST"))
                .thenReturn(Optional.empty());

            assertThrows(Exception.class, () ->
                saleModelAppService.getAvailableConfigPolicies("NON_EXIST", VARIANT_CODE));
        }

        @Test
        @DisplayName("销售车型无 carlineCode 时返回空列表")
        void should_return_empty_list_when_no_carline_code() {
            SaleModelPo saleModel = SaleModelPo.builder()
                .saleModelCode(SALE_MODEL_CODE)
                .carlineCode(null)
                .build();

            when(saleModelRepository.findBySaleModelCode(SALE_MODEL_CODE))
                .thenReturn(Optional.of(saleModel));

            List<ConfigPolicyAvailableVo> result = saleModelAppService.getAvailableConfigPolicies(SALE_MODEL_CODE, VARIANT_CODE);

            assertTrue(result.isEmpty());
            verify(mdmProjectionService, never()).getConfigurationsByVariant(any());
        }
    }

    @Nested
    @DisplayName("deleteConfigPolicy 方法")
    class DeleteConfigPolicyTest {

        @Test
        @DisplayName("删除存在的 Configuration 白名单成功")
        void should_delete_existing_config_policy() {
            SaleModelConfigPolicyPo existingPo = SaleModelConfigPolicyPo.builder()
                .id(1L)
                .saleModelCode(SALE_MODEL_CODE)
                .configurationCode(CONFIG_CODE)
                .build();

            when(configPolicyRepository.findBySaleModelCodeAndConfigCode(SALE_MODEL_CODE, CONFIG_CODE))
                .thenReturn(Optional.of(existingPo));

            int result = saleModelAppService.deleteConfigPolicy(SALE_MODEL_CODE, CONFIG_CODE);

            assertEquals(1, result);
            verify(configPolicyRepository).delete(1L);
        }

        @Test
        @DisplayName("删除不存在的 Configuration 白名单返回 0")
        void should_return_0_when_config_not_found() {
            when(configPolicyRepository.findBySaleModelCodeAndConfigCode(SALE_MODEL_CODE, CONFIG_CODE))
                .thenReturn(Optional.empty());

            int result = saleModelAppService.deleteConfigPolicy(SALE_MODEL_CODE, CONFIG_CODE);

            assertEquals(0, result);
            verify(configPolicyRepository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("createOptionPolicy 方法")
    class CreateOptionPolicyTest {

        @Test
        @DisplayName("创建 Option 销售策略成功")
        void should_create_option_policy_successfully() {
            CreateOptionPolicyCmd cmd = CreateOptionPolicyCmd.builder()
                .saleModelCode(SALE_MODEL_CODE)
                .optionCode(OPTION_CODE)
                .optionFamilyCode("COLOR")
                .saleStatus("active")
                .optionPrice(BigDecimal.valueOf(5000))
                .build();

            SaleModelOptionPolicyPo result = saleModelAppService.createOptionPolicy(cmd);

            assertNotNull(result);
            assertEquals(SALE_MODEL_CODE, result.getSaleModelCode());
            assertEquals(OPTION_CODE, result.getOptionCode());
            assertEquals("COLOR", result.getOptionFamilyCode());
            assertEquals("active", result.getSaleStatus());
            assertEquals(BigDecimal.valueOf(5000), result.getOptionPrice());
            verify(optionPolicyRepository).save(result);
        }

        @Test
        @DisplayName("未指定销售状态时默认使用 active")
        void should_use_default_sale_status_active() {
            CreateOptionPolicyCmd cmd = CreateOptionPolicyCmd.builder()
                .saleModelCode(SALE_MODEL_CODE)
                .optionCode(OPTION_CODE)
                .optionFamilyCode("COLOR")
                .saleStatus(null)
                .optionPrice(BigDecimal.valueOf(5000))
                .build();

            SaleModelOptionPolicyPo result = saleModelAppService.createOptionPolicy(cmd);

            assertEquals("active", result.getSaleStatus());
        }
    }

    @Nested
    @DisplayName("updateOptionPolicy 方法")
    class UpdateOptionPolicyTest {

        @Test
        @DisplayName("更新 Option 销售策略成功")
        void should_update_option_policy_successfully() {
            CreateOptionPolicyCmd cmd = CreateOptionPolicyCmd.builder()
                .saleModelCode(SALE_MODEL_CODE)
                .optionCode(OPTION_CODE)
                .optionFamilyCode("COLOR")
                .saleStatus("off_shelf")
                .optionPrice(BigDecimal.valueOf(6000))
                .build();

            SaleModelOptionPolicyPo existingPo = SaleModelOptionPolicyPo.builder()
                .id(1L)
                .saleModelCode(SALE_MODEL_CODE)
                .optionCode(OPTION_CODE)
                .optionFamilyCode("COLOR")
                .saleStatus("active")
                .optionPrice(BigDecimal.valueOf(5000))
                .build();

            when(optionPolicyRepository.findById(1L))
                .thenReturn(Optional.of(existingPo));

            SaleModelOptionPolicyPo result = saleModelAppService.updateOptionPolicy(1L, cmd);

            assertNotNull(result);
            assertEquals("off_shelf", result.getSaleStatus());
            assertEquals(BigDecimal.valueOf(6000), result.getOptionPrice());
            verify(optionPolicyRepository).update(existingPo);
        }

        @Test
        @DisplayName("更新不存在的 Option 销售策略应抛出异常")
        void should_throw_when_option_policy_not_found() {
            CreateOptionPolicyCmd cmd = CreateOptionPolicyCmd.builder()
                .saleModelCode(SALE_MODEL_CODE)
                .optionCode(OPTION_CODE)
                .build();

            when(optionPolicyRepository.findById(999L))
                .thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class, () -> 
                saleModelAppService.updateOptionPolicy(999L, cmd));
        }
    }

    @Nested
    @DisplayName("deleteOptionPolicy 方法")
    class DeleteOptionPolicyTest {

        @Test
        @DisplayName("删除 Option 销售策略成功")
        void should_delete_option_policy_successfully() {
            int result = saleModelAppService.deleteOptionPolicy(1L);

            assertEquals(1, result);
            verify(optionPolicyRepository).delete(1L);
        }
    }
}
