package net.hwyz.iov.cloud.otd.vso.service.facade.assembler;

import net.hwyz.iov.cloud.otd.vso.api.contract.SaleModel;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.po.SaleModelPo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 销售车型转换类
 *
 * @author hwyz_leo
 */
@Mapper
public interface SaleModelResponseAssembler {

    SaleModelResponseAssembler INSTANCE = Mappers.getMapper(SaleModelResponseAssembler.class);

    /**
     * 数据对象转数据传输对象
     *
     * @param saleModelPo 数据对象
     * @return 数据传输对象
     */
    @Mappings({
            @Mapping(target = "saleImage", expression = "java(cn.hutool.json.JSONUtil.toBean(saleModelPo.getSaleImage(), new cn.hutool.core.lang.TypeReference<List<String>>() {}, true))")
    })
    SaleModel fromPo(SaleModelPo saleModelPo);

    /**
     * 数据对象列表转数据传输对象列表
     *
     * @param saleModelPoList 数据对象列表
     * @return 数据传输对象列表
     */
    List<SaleModel> fromPoList(List<SaleModelPo> saleModelPoList);

}
