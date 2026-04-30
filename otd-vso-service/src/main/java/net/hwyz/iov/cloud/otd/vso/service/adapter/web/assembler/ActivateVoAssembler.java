package net.hwyz.iov.cloud.otd.vso.service.adapter.web.assembler;

import net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo.ActivateVo;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.ActivateCmd;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * ActivateVo Assembler
 *
 * @author VSO Team
 */
@Mapper
public interface ActivateVoAssembler {

    ActivateVoAssembler INSTANCE = Mappers.getMapper(ActivateVoAssembler.class);

    ActivateCmd toCmd(ActivateVo vo);

}
