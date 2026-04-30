package net.hwyz.iov.cloud.otd.vso.service.adapter.web.assembler;

import net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo.PrepareTransportVo;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.PrepareTransportCmd;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * PrepareTransportVo Assembler
 *
 * @author VSO Team
 */
@Mapper
public interface PrepareTransportVoAssembler {

    PrepareTransportVoAssembler INSTANCE = Mappers.getMapper(PrepareTransportVoAssembler.class);

    PrepareTransportCmd toCmd(PrepareTransportVo vo);

}
