package net.hwyz.iov.cloud.otd.vso.service.adapter.web.assembler;

import net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo.SelectedSaleModelRequestVo;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.CreateWishlistCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.ModifyWishlistCmd;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * SelectedSaleModelRequestVo Assembler
 *
 * @author VSO Team
 */
@Mapper
public interface SelectedSaleModelRequestVoAssembler {

    SelectedSaleModelRequestVoAssembler INSTANCE = Mappers.getMapper(SelectedSaleModelRequestVoAssembler.class);

    @Mapping(target = "accountId", source = "accountId")
    CreateWishlistCmd toCreateWishlistCmd(String accountId, SelectedSaleModelRequestVo vo);

    @Mapping(target = "accountId", source = "accountId")
    ModifyWishlistCmd toModifyWishlistCmd(String accountId, SelectedSaleModelRequestVo vo);

}
