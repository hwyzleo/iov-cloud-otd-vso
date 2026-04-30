package net.hwyz.iov.cloud.otd.vso.service.adapter.web.assembler;

import net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo.SaleModelConfigMpt;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.SaleModelConfigResult;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.Map;

/**
 * SaleModelConfigMpt VO Assembler
 *
 * @author VSO Team
 */
@Mapper
public interface SaleModelConfigMptAssembler {

    SaleModelConfigMptAssembler INSTANCE = Mappers.getMapper(SaleModelConfigMptAssembler.class);

    SaleModelConfigMpt toVo(SaleModelConfigResult result);

    Map<String, SaleModelConfigMpt> toVoMap(Map<String, SaleModelConfigResult> resultMap);

}
