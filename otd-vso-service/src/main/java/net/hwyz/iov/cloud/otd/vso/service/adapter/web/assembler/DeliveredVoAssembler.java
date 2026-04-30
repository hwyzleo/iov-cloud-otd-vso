package net.hwyz.iov.cloud.otd.vso.service.adapter.web.assembler;

import net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo.DeliveredVo;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.DeliveredCmd;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * DeliveredVo Assembler
 *
 * @author VSO Team
 */
@Mapper
public interface DeliveredVoAssembler {

    DeliveredVoAssembler INSTANCE = Mappers.getMapper(DeliveredVoAssembler.class);

    DeliveredCmd toCmd(DeliveredVo vo);

}
