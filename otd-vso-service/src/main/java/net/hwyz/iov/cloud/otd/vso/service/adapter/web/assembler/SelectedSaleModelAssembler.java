package net.hwyz.iov.cloud.otd.vso.service.adapter.web.assembler;

import net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo.SelectedSaleModel;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.SelectedSaleModelResult;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * SelectedSaleModel Assembler
 *
 * @author VSO Team
 */
@Mapper
public interface SelectedSaleModelAssembler {

    SelectedSaleModelAssembler INSTANCE = Mappers.getMapper(SelectedSaleModelAssembler.class);

    SelectedSaleModel toVo(SelectedSaleModelResult result);

}
