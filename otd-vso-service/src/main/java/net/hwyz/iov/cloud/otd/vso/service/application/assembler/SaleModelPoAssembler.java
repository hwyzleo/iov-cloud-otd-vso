package net.hwyz.iov.cloud.otd.vso.service.application.assembler;

import net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo.SaleModelConfigDto;
import net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo.SaleModelCreateDto;
import net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo.SaleModelUpdateDto;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelConfigPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelPo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * 销售车型 PO Assembler
 *
 * @author VSO Team
 */
@Mapper
public interface SaleModelPoAssembler {

    SaleModelPoAssembler INSTANCE = Mappers.getMapper(SaleModelPoAssembler.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "modifyTime", ignore = true)
    @Mapping(target = "modifyBy", ignore = true)
    @Mapping(target = "description", ignore = true)
    @Mapping(target = "rowVersion", ignore = true)
    @Mapping(target = "rowValid", ignore = true)
    @Mapping(target = "images", expression = "java(dto.getImages() != null ? String.join(\",\", dto.getImages()) : null)")
    SaleModelPo toDo(SaleModelCreateDto dto);

    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "modifyTime", ignore = true)
    @Mapping(target = "modifyBy", ignore = true)
    @Mapping(target = "description", ignore = true)
    @Mapping(target = "rowVersion", ignore = true)
    @Mapping(target = "rowValid", ignore = true)
    @Mapping(target = "parameters", ignore = true)
    @Mapping(target = "images", expression = "java(dto.getImages() != null ? String.join(\",\", dto.getImages()) : null)")
    SaleModelPo toUpdateDo(SaleModelUpdateDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "saleModelCode", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "modifyTime", ignore = true)
    @Mapping(target = "modifyBy", ignore = true)
    @Mapping(target = "description", ignore = true)
    @Mapping(target = "rowVersion", ignore = true)
    @Mapping(target = "rowValid", ignore = true)
    @Mapping(target = "typeImage", expression = "java(dto.getTypeImage() != null ? cn.hutool.json.JSONUtil.toJsonStr(dto.getTypeImage()) : null)")
    SaleModelConfigPo toConfigDo(SaleModelConfigDto dto);

}
