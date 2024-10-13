package net.hwyz.iov.cloud.otd.vso.service.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.otd.vso.api.contract.SaleModelConfig;
import net.hwyz.iov.cloud.otd.vso.service.facade.assembler.SaleModelConfigAssembler;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.dao.SaleModelConfigDao;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.po.SaleModelConfigPo;
import net.hwyz.iov.cloud.tsp.framework.commons.enums.Symbol;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 销售车型应用服务类
 *
 * @author hwyz_leo
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SaleModelAppService {

    private final SaleModelConfigDao saleModelConfigDao;

    /**
     * 获取销售车型列表
     *
     * @param saleCode 销售代码
     * @return 销售车型列表
     */
    public List<SaleModelConfig> getSaleModelResponse(String saleCode) {
        return SaleModelConfigAssembler.INSTANCE.fromPoList(getSaleModelConfigList(saleCode));
    }

    /**
     * 获取销售车型Map
     *
     * @param saleCode 销售代码
     * @return 销售车型Map key:销售车型类型_销售车型类型代码 value:销售车型Po
     */
    public Map<String, SaleModelConfigPo> getSaleModelConfigMap(String saleCode) {
        return getSaleModelConfigList(saleCode).stream().collect(Collectors.toMap(k -> k.getType() + Symbol.UNDERSCORE.value + k.getTypeCode(), v -> v));
    }

    /**
     * 获取销售车型配置列表
     *
     * @param saleCode 销售代码
     * @return 销售车型列表
     */
    private List<SaleModelConfigPo> getSaleModelConfigList(String saleCode) {
        return saleModelConfigDao.selectPoByExample(SaleModelConfigPo.builder().saleCode(saleCode).build());
    }

}
