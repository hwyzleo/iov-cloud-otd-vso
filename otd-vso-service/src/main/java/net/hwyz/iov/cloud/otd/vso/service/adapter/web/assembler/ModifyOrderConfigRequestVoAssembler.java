package net.hwyz.iov.cloud.otd.vso.service.adapter.web.assembler;

import net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo.ModifyOrderConfigRequestVo;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.ModifyOrderConfigCmd;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * ModifyOrderConfigRequestVo Assembler
 *
 * @author VSO Team
 */
@Mapper
public interface ModifyOrderConfigRequestVoAssembler {

    ModifyOrderConfigRequestVoAssembler INSTANCE = Mappers.getMapper(ModifyOrderConfigRequestVoAssembler.class);

    @Mapping(target = "accountId", source = "accountId")
    @Mapping(target = "orderNo", source = "vo.orderNo")
    @Mapping(target = "optionCodes", source = "vo.optionCodes")
    ModifyOrderConfigCmd toCmd(String accountId, ModifyOrderConfigRequestVo vo);

}
