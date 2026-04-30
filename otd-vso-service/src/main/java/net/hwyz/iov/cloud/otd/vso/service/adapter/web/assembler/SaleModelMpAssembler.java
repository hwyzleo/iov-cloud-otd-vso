package net.hwyz.iov.cloud.otd.vso.service.adapter.web.assembler;

import net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo.SaleModelMp;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.SaleModelResult;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * SaleModelMp VO Assembler
 *
 * @author VSO Team
 */
@Mapper
public interface SaleModelMpAssembler {

    SaleModelMpAssembler INSTANCE = Mappers.getMapper(SaleModelMpAssembler.class);

    SaleModelMp toVo(SaleModelResult result);

    List<SaleModelMp> toVoList(List<SaleModelResult> resultList);

}
