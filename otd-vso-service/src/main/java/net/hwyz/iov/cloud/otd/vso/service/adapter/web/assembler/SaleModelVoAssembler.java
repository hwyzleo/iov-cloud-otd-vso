package net.hwyz.iov.cloud.otd.vso.service.adapter.web.assembler;

import net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo.SaleModelVo;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.SaleModelResult;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.SelectedSaleModelResult;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * SaleModelVo Assembler
 *
 * @author VSO Team
 */
@Mapper
public interface SaleModelVoAssembler {

    SaleModelVoAssembler INSTANCE = Mappers.getMapper(SaleModelVoAssembler.class);

    SaleModelVo toVo(SaleModelResult result);

    List<SaleModelVo> toVoList(List<SaleModelResult> resultList);

    SaleModelVo toVo(SelectedSaleModelResult result);

}
