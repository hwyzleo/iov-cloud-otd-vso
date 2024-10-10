package net.hwyz.iov.cloud.otd.vso.service.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.otd.vso.api.contract.response.SaleModelResponse;
import net.hwyz.iov.cloud.otd.vso.service.facade.assembler.SaleModelResponseAssembler;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.dao.SaleModelDao;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.po.SaleModelPo;
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

    private final SaleModelDao saleModelDao;

    /**
     * 获取销售车型列表
     *
     * @param saleCode 销售代码
     * @return 销售车型列表
     */
    public SaleModelResponse getSaleModelResponse(String saleCode) {
        return SaleModelResponse.builder()
                .saleModels(SaleModelResponseAssembler.INSTANCE.fromPoList(getSaleModelList(saleCode)))
                .build();
    }

    /**
     * 获取销售车型Map
     *
     * @param saleCode 销售代码
     * @return 销售车型Map key:销售车型类型_销售车型类型代码 value:销售车型Po
     */
    public Map<String, SaleModelPo> getSaleModelMap(String saleCode) {
        return getSaleModelList(saleCode).stream().collect(Collectors.toMap(k -> k.getSaleModelType() + Symbol.UNDERSCORE.value + k.getSaleModelTypeCode(), v -> v));
    }

    /**
     * 获取销售车型列表
     *
     * @param saleCode 销售代码
     * @return 销售车型列表
     */
    private List<SaleModelPo> getSaleModelList(String saleCode) {
        return saleModelDao.selectPoByExample(SaleModelPo.builder().saleCode(saleCode).build());
    }

}
