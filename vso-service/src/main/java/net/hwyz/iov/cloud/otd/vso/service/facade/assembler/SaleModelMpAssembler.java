package net.hwyz.iov.cloud.otd.vso.service.facade.assembler;

import net.hwyz.iov.cloud.otd.vso.api.contract.SaleModelMp;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.po.SaleModelPo;
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
public interface SaleModelMpAssembler {

    SaleModelMpAssembler INSTANCE = Mappers.getMapper(SaleModelMpAssembler.class);

    /**
     * 数据对象转数据传输对象
     *
     * @param saleModelPo 数据对象
     * @return 数据传输对象
     */
    @Mappings({
            @Mapping(target = "images", expression = "java(cn.hutool.json.JSONUtil.toList(saleModelPo.getImages(), String.class))")
    })
    SaleModelMp fromPo(SaleModelPo saleModelPo);

    /**
     * 数据对象列表转数据传输对象列表
     *
     * @param saleModelPoList 数据对象列表
     * @return 数据传输对象列表
     */
    List<SaleModelMp> fromPoList(List<SaleModelPo> saleModelPoList);

}
