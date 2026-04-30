package net.hwyz.iov.cloud.otd.vso.service.adapter.web.assembler;

import net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo.ApplyTransportVo;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.ApplyTransportCmd;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * ApplyTransportVo Assembler
 *
 * @author VSO Team
 */
@Mapper
public interface ApplyTransportVoAssembler {

    ApplyTransportVoAssembler INSTANCE = Mappers.getMapper(ApplyTransportVoAssembler.class);

    ApplyTransportCmd toCmd(ApplyTransportVo vo);

}
