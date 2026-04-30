package net.hwyz.iov.cloud.otd.vso.service.adapter.web.assembler;

import net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo.SaleModelConfigVo;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.SaleModelConfigResult;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * SaleModelConfigVo Assembler
 *
 * @author VSO Team
 */
@Mapper
public interface SaleModelConfigVoAssembler {

    SaleModelConfigVoAssembler INSTANCE = Mappers.getMapper(SaleModelConfigVoAssembler.class);

    SaleModelConfigVo toVo(SaleModelConfigResult result);

    List<SaleModelConfigVo> toVoList(List<SaleModelConfigResult> resultList);

}
