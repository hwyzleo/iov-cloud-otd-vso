package net.hwyz.iov.cloud.otd.vso.service.application.service;

import net.hwyz.iov.cloud.otd.vso.service.BaseTest;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.CreateConfigPolicyCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.CreateOptionPolicyCmd;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.SaleModelConfigPolicyRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.SaleModelOptionPolicyRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelConfigPolicyPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelOptionPolicyPo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SaleModelAppService - 销售策略集成测试")
@Transactional
class SaleModelAppServicePolicyIntegrationTest extends BaseTest {

    @Autowired
    private SaleModelAppService saleModelAppService;

    @Autowired
    private SaleModelConfigPolicyRepository configPolicyRepository;

    @Autowired
    private SaleModelOptionPolicyRepository optionPolicyRepository;

    private static final String SALE_MODEL_CODE = "TEST_MODEL_" + System.currentTimeMillis();

    @Nested
    @DisplayName("createConfigPolicy 方法")
    class CreateConfigPolicyTest {

        @Test
        @DisplayName("创建 Configuration 白名单 - 验证数据库写入")
        void should_create_config_policy_and_persist_to_db() {
            String configCode1 = "CONFIG_" + System.currentTimeMillis() + "_1";
            String configCode2 = "CONFIG_" + System.currentTimeMillis() + "_2";

            CreateConfigPolicyCmd cmd = CreateConfigPolicyCmd.builder()
                .saleModelCode(SALE_MODEL_CODE)
                .configurationCodes(Arrays.asList(configCode1, configCode2))
                .status("active")
                .build();

            List<SaleModelConfigPolicyPo> result = saleModelAppService.createConfigPolicy(cmd);

            assertEquals(2, result.size());

            // 验证数据库写入
            Optional<SaleModelConfigPolicyPo> saved1 = configPolicyRepository
                .findBySaleModelCodeAndConfigCode(SALE_MODEL_CODE, configCode1);
            assertTrue(saved1.isPresent());
            assertEquals("active", saved1.get().getStatus());

            Optional<SaleModelConfigPolicyPo> saved2 = configPolicyRepository
                .findBySaleModelCodeAndConfigCode(SALE_MODEL_CODE, configCode2);
            assertTrue(saved2.isPresent());
        }

        @Test
        @DisplayName("重复创建应 reactivate - 验证数据库更新")
        void should_reactivate_existing_config_policy() {
            String configCode = "CONFIG_" + System.currentTimeMillis();

            // 首次创建
            CreateConfigPolicyCmd cmd1 = CreateConfigPolicyCmd.builder()
                .saleModelCode(SALE_MODEL_CODE)
                .configurationCodes(Arrays.asList(configCode))
                .status("active")
                .build();
            saleModelAppService.createConfigPolicy(cmd1);

            // 再次创建（应 reactivate）
            CreateConfigPolicyCmd cmd2 = CreateConfigPolicyCmd.builder()
                .saleModelCode(SALE_MODEL_CODE)
                .configurationCodes(Arrays.asList(configCode))
                .status("active")
                .build();
            List<SaleModelConfigPolicyPo> result = saleModelAppService.createConfigPolicy(cmd2);

            assertEquals(1, result.size());

            // 验证数据库中只有一条记录
            Optional<SaleModelConfigPolicyPo> saved = configPolicyRepository
                .findBySaleModelCodeAndConfigCode(SALE_MODEL_CODE, configCode);
            assertTrue(saved.isPresent());
            assertEquals("active", saved.get().getStatus());
        }
    }

    @Nested
    @DisplayName("deleteConfigPolicy 方法")
    class DeleteConfigPolicyTest {

        @Test
        @DisplayName("删除 Configuration 白名单 - 验证数据库删除")
        void should_delete_config_policy_from_db() {
            String configCode = "CONFIG_" + System.currentTimeMillis();

            // 先创建
            CreateConfigPolicyCmd cmd = CreateConfigPolicyCmd.builder()
                .saleModelCode(SALE_MODEL_CODE)
                .configurationCodes(Arrays.asList(configCode))
                .status("active")
                .build();
            saleModelAppService.createConfigPolicy(cmd);

            // 删除
            int deleted = saleModelAppService.deleteConfigPolicy(SALE_MODEL_CODE, configCode);
            assertEquals(1, deleted);

            // 验证数据库逻辑删除（row_valid=0）
            Optional<SaleModelConfigPolicyPo> saved = configPolicyRepository
                .findBySaleModelCodeAndConfigCode(SALE_MODEL_CODE, configCode);
            assertTrue(saved.isPresent());
            assertFalse(saved.get().getRowValid());
        }
    }

    @Nested
    @DisplayName("createOptionPolicy 方法")
    class CreateOptionPolicyTest {

        @Test
        @DisplayName("创建 Option 销售策略 - 验证数据库写入")
        void should_create_option_policy_and_persist_to_db() {
            String optionCode = "OPT_" + System.currentTimeMillis();

            CreateOptionPolicyCmd cmd = CreateOptionPolicyCmd.builder()
                .saleModelCode(SALE_MODEL_CODE)
                .optionCode(optionCode)
                .optionFamilyCode("COLOR")
                .saleStatus("active")
                .optionPrice(BigDecimal.valueOf(5000))
                .marketingTitle("测试颜色")
                .sortWeight(100)
                .build();

            SaleModelOptionPolicyPo result = saleModelAppService.createOptionPolicy(cmd);

            assertNotNull(result);
            assertNotNull(result.getId());

            // 验证数据库写入
            SaleModelOptionPolicyPo saved = optionPolicyRepository.findById(result.getId()).orElse(null);
            assertNotNull(saved);
            assertEquals(SALE_MODEL_CODE, saved.getSaleModelCode());
            assertEquals(optionCode, saved.getOptionCode());
            assertEquals("COLOR", saved.getOptionFamilyCode());
            assertEquals("active", saved.getSaleStatus());
            assertEquals(0, BigDecimal.valueOf(5000).compareTo(saved.getOptionPrice()));
            assertEquals("测试颜色", saved.getMarketingTitle());
            assertEquals(100, saved.getSortWeight());
        }
    }

    @Nested
    @DisplayName("updateOptionPolicy 方法")
    class UpdateOptionPolicyTest {

        @Test
        @DisplayName("更新 Option 销售策略 - 验证数据库更新")
        void should_update_option_policy_and_persist_to_db() {
            String optionCode = "OPT_" + System.currentTimeMillis();

            // 先创建
            CreateOptionPolicyCmd createCmd = CreateOptionPolicyCmd.builder()
                .saleModelCode(SALE_MODEL_CODE)
                .optionCode(optionCode)
                .optionFamilyCode("COLOR")
                .saleStatus("active")
                .optionPrice(BigDecimal.valueOf(5000))
                .build();
            SaleModelOptionPolicyPo created = saleModelAppService.createOptionPolicy(createCmd);

            // 更新
            CreateOptionPolicyCmd updateCmd = CreateOptionPolicyCmd.builder()
                .saleModelCode(SALE_MODEL_CODE)
                .optionCode(optionCode)
                .optionFamilyCode("COLOR")
                .saleStatus("off_shelf")
                .optionPrice(BigDecimal.valueOf(6000))
                .marketingTitle("更新后的标题")
                .build();
            saleModelAppService.updateOptionPolicy(created.getId(), updateCmd);

            // 验证数据库更新
            SaleModelOptionPolicyPo updated = optionPolicyRepository.findById(created.getId()).orElse(null);
            assertNotNull(updated);
            assertEquals("off_shelf", updated.getSaleStatus());
            assertEquals(0, BigDecimal.valueOf(6000).compareTo(updated.getOptionPrice()));
            assertEquals("更新后的标题", updated.getMarketingTitle());
        }
    }

    @Nested
    @DisplayName("deleteOptionPolicy 方法")
    class DeleteOptionPolicyTest {

        @Test
        @DisplayName("删除 Option 销售策略 - 验证数据库删除")
        void should_delete_option_policy_from_db() {
            String optionCode = "OPT_" + System.currentTimeMillis();

            // 先创建
            CreateOptionPolicyCmd cmd = CreateOptionPolicyCmd.builder()
                .saleModelCode(SALE_MODEL_CODE)
                .optionCode(optionCode)
                .optionFamilyCode("COLOR")
                .saleStatus("active")
                .optionPrice(BigDecimal.valueOf(5000))
                .build();
            SaleModelOptionPolicyPo created = saleModelAppService.createOptionPolicy(cmd);

            // 删除
            saleModelAppService.deleteOptionPolicy(created.getId());

            // 验证数据库删除
            assertFalse(optionPolicyRepository.findById(created.getId()).isPresent());
        }
    }
}
