package net.hwyz.iov.cloud.otd.vso.service.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.ConfigurationNotForSaleException;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.OptionNotForSaleException;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.SaleModelConfigPolicyRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.SaleModelOptionPolicyRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelConfigPolicyPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelOptionPolicyPo;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * 销售策略校验服务
 *
 * @author hwyz_leo
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SalesPolicyService {

    private final SaleModelConfigPolicyRepository configPolicyRepository;
    private final SaleModelOptionPolicyRepository optionPolicyRepository;

    /**
     * 校验 Configuration 是否在销售白名单中
     * 白名单语义：表中无任何行 = ALL 全开；表中只要有 1 行 = 严格白名单语义
     */
    public void validateConfigurationForSale(String saleModelCode, String configurationCode) {
        List<SaleModelConfigPolicyPo> policies = configPolicyRepository.findBySaleModelCode(saleModelCode);

        // 空表 = ALL 全开
        if (policies.isEmpty()) {
            log.debug("Configuration 白名单为空，ALL 全开，saleModelCode: {}", saleModelCode);
            return;
        }

        // 严格白名单语义
        SaleModelConfigPolicyPo policy = configPolicyRepository
            .findBySaleModelCodeAndConfigCode(saleModelCode, configurationCode)
            .orElseThrow(() -> new ConfigurationNotForSaleException(
                String.format("Configuration [%s] 未列入销售白名单", configurationCode)));

        if (!"active".equals(policy.getStatus())) {
            throw new ConfigurationNotForSaleException(
                String.format("Configuration [%s] 状态为 [%s]，不可售", configurationCode, policy.getStatus()));
        }

        log.debug("Configuration [{}] 校验通过", configurationCode);
    }

    /**
     * 校验 OptionCode 是否在销售策略中且可售
     */
    public void validateOptionForSale(String saleModelCode, String optionCode, String regionCode) {
        SaleModelOptionPolicyPo policy = optionPolicyRepository
            .findBySaleModelCodeAndOptionCode(saleModelCode, optionCode)
            .orElseThrow(() -> new OptionNotForSaleException(
                String.format("OptionCode [%s] 未配置销售策略", optionCode)));

        // 校验销售状态
        if (!"active".equals(policy.getSaleStatus())) {
            throw new OptionNotForSaleException(
                String.format("OptionCode [%s] 销售状态为 [%s]", optionCode, policy.getSaleStatus()));
        }

        // 校验价格
        if (policy.getOptionPrice() == null) {
            throw new OptionNotForSaleException(
                String.format("OptionCode [%s] 未配置价格", optionCode));
        }

        // 校验区域（如果配置了区域限制）
        if (policy.getAvailableRegions() != null && !policy.getAvailableRegions().isEmpty()) {
            // TODO: 解析 JSON 并校验 regionCode 是否在列表中
            // 暂时跳过区域校验
        }

        log.debug("OptionCode [{}] 校验通过", optionCode);
    }

    /**
     * 批量校验 OptionCode 列表
     */
    public void validateOptionsForSale(String saleModelCode, List<String> optionCodes, String regionCode) {
        for (String optionCode : optionCodes) {
            validateOptionForSale(saleModelCode, optionCode, regionCode);
        }
    }

    /**
     * 获取 OptionCode 的价格
     */
    public java.math.BigDecimal getOptionPrice(String saleModelCode, String optionCode) {
        SaleModelOptionPolicyPo policy = optionPolicyRepository
            .findBySaleModelCodeAndOptionCode(saleModelCode, optionCode)
            .orElse(null);

        if (policy == null || !"active".equals(policy.getSaleStatus()) || policy.getOptionPrice() == null) {
            return java.math.BigDecimal.ZERO;
        }

        return policy.getOptionPrice();
    }
}
