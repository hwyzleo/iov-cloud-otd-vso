package net.hwyz.iov.cloud.otd.vso.service.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.SaleModelOptionPolicyRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.MdmProjectionVariantPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelOptionPolicyPo;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 选配器服务
 *
 * @author hwyz_leo
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConfiguratorService {

    private final MdmProjectionService mdmProjectionService;
    private final SalesPolicyService salesPolicyService;
    private final SaleModelOptionPolicyRepository optionPolicyRepository;

    /**
     * 获取选配器数据
     * 返回 Variant 信息、标配 options、可选 families
     */
    public ConfiguratorData getConfigurator(String variantCode, String saleModelCode, String regionCode) {
        // 1. 获取 Variant 投影
        MdmProjectionVariantPo variant = mdmProjectionService.getVariant(variantCode);

        // 2. 获取该 SaleModel 下的 OptionCode 销售策略
        List<SaleModelOptionPolicyPo> policies = optionPolicyRepository.findBySaleModelCode(saleModelCode);

        // 3. 过滤可售的 OptionCode
        Map<String, List<SaleModelOptionPolicyPo>> familyPolicies = policies.stream()
            .filter(p -> "active".equals(p.getSaleStatus()))
            .filter(p -> p.getOptionPrice() != null)
            // TODO: 区域过滤
            .collect(Collectors.groupingBy(SaleModelOptionPolicyPo::getOptionFamilyCode));

        // 4. 移除空 family
        familyPolicies.entrySet().removeIf(entry -> entry.getValue().isEmpty());

        return new ConfiguratorData(variant, familyPolicies);
    }

    /**
     * 计算总价
     */
    public BigDecimal calculateTotalPrice(String saleModelCode, BigDecimal basePrice, List<String> optionCodes) {
        BigDecimal optionTotal = optionCodes.stream()
            .map(code -> salesPolicyService.getOptionPrice(saleModelCode, code))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return basePrice.add(optionTotal);
    }

    /**
     * 选配器数据
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class ConfiguratorData {
        private MdmProjectionVariantPo variant;
        private Map<String, List<SaleModelOptionPolicyPo>> familyPolicies;
    }
}
