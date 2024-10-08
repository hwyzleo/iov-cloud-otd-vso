package net.hwyz.iov.cloud.otd.vso.service.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.otd.vso.api.contract.SaleModel;
import net.hwyz.iov.cloud.otd.vso.api.contract.response.SaleModelResponse;
import net.hwyz.iov.cloud.otd.vso.service.facade.assembler.SaleModelResponseAssembler;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.dao.SaleModelDao;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.po.SaleModelPo;
import org.springframework.stereotype.Service;

import java.util.List;

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
    public SaleModelResponse getSaleModelList(String saleCode) {
        List<SaleModelPo> saleModelPoList = saleModelDao.selectPoByExample(SaleModelPo.builder().saleCode(saleCode).build());
        return SaleModelResponse.builder()
                .saleModels(SaleModelResponseAssembler.INSTANCE.fromPoList(saleModelPoList))
                .build();
    }

}
