package net.hwyz.iov.cloud.otd.vso.service.facade.assembler;

import net.hwyz.iov.cloud.otd.vso.api.contract.SaleModelConfig;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.po.SaleModelConfigPo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 销售车型配置转换类
 *
 * @author hwyz_leo
 */
@Mapper
public interface SaleModelConfigAssembler {

    SaleModelConfigAssembler INSTANCE = Mappers.getMapper(SaleModelConfigAssembler.class);

    /**
     * 数据对象转数据传输对象
     *
     * @param saleModelConfigPo 数据对象
     * @return 数据传输对象
     */
    @Mappings({
            @Mapping(target = "typeImage", expression = "java(cn.hutool.json.JSONUtil.toBean(saleModelConfigPo.getTypeImage(), new cn.hutool.core.lang.TypeReference<List<String>>() {}, true))")
    })
    SaleModelConfig fromPo(SaleModelConfigPo saleModelConfigPo);

    /**
     * 数据对象列表转数据传输对象列表
     *
     * @param saleModelPoList 数据对象列表
     * @return 数据传输对象列表
     */
    List<SaleModelConfig> fromPoList(List<SaleModelConfigPo> saleModelPoList);

}
