package net.hwyz.iov.cloud.otd.vso.service.adapter.web.assembler;

import net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo.PrepareDeliveryVo;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.PrepareDeliveryCmd;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * PrepareDeliveryVo Assembler
 *
 * @author VSO Team
 */
@Mapper
public interface PrepareDeliveryVoAssembler {

    PrepareDeliveryVoAssembler INSTANCE = Mappers.getMapper(PrepareDeliveryVoAssembler.class);

    PrepareDeliveryCmd toCmd(PrepareDeliveryVo vo);

}
