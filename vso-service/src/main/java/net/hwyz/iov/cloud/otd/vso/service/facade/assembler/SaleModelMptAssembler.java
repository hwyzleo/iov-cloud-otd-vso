package net.hwyz.iov.cloud.otd.vso.service.facade.assembler;

import net.hwyz.iov.cloud.otd.vso.api.contract.SaleModelMpt;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.po.SaleModelPo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 管理后台销售车型转换类
 *
 * @author hwyz_leo
 */
@Mapper
public interface SaleModelMptAssembler {

    SaleModelMptAssembler INSTANCE = Mappers.getMapper(SaleModelMptAssembler.class);

    /**
     * 数据对象转数据传输对象
     *
     * @param saleModelPo 数据对象
     * @return 数据传输对象
     */
    @Mappings({
            @Mapping(target = "images", expression = "java(cn.hutool.json.JSONUtil.toList(saleModelPo.getImages(), String.class))")
    })
    SaleModelMpt fromPo(SaleModelPo saleModelPo);

    /**
     * 数据传输对象转数据对象
     *
     * @param saleModelMpt 数据传输对象
     * @return 数据对象
     */
    @Mappings({
            @Mapping(target = "images", expression = "java(cn.hutool.json.JSONUtil.toJsonStr(saleModelMpt.getImages()))")
    })
    SaleModelPo toPo(SaleModelMpt saleModelMpt);

    /**
     * 数据对象列表转数据传输对象列表
     *
     * @param saleModelPoList 数据对象列表
     * @return 数据传输对象列表
     */
    List<SaleModelMpt> fromPoList(List<SaleModelPo> saleModelPoList);

}
