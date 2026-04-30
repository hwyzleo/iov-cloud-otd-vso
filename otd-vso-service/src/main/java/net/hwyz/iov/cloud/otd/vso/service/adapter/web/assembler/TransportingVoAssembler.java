package net.hwyz.iov.cloud.otd.vso.service.adapter.web.assembler;

import net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo.TransportingVo;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.TransportingCmd;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * TransportingVo Assembler
 *
 * @author VSO Team
 */
@Mapper
public interface TransportingVoAssembler {

    TransportingVoAssembler INSTANCE = Mappers.getMapper(TransportingVoAssembler.class);

    TransportingCmd toCmd(TransportingVo vo);

}
