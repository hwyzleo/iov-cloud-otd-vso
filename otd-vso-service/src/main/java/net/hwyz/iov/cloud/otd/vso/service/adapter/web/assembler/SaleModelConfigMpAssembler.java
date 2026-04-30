package net.hwyz.iov.cloud.otd.vso.service.adapter.web.assembler;

import net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo.SaleModelConfigMp;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.SaleModelConfigResult;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * SaleModelConfigMp VO Assembler
 *
 * @author VSO Team
 */
@Mapper
public interface SaleModelConfigMpAssembler {

    SaleModelConfigMpAssembler INSTANCE = Mappers.getMapper(SaleModelConfigMpAssembler.class);

    SaleModelConfigMp toVo(SaleModelConfigResult result);

    List<SaleModelConfigMp> toVoList(List<SaleModelConfigResult> resultList);

}
