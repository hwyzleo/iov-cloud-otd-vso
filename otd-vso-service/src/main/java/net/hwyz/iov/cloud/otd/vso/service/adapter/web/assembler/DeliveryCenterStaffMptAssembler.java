package net.hwyz.iov.cloud.otd.vso.service.adapter.web.assembler;

import net.hwyz.iov.cloud.otd.vso.api.vo.mpt.DeliveryCenterStaffMpt;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.DeliveryStaffResult;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * DeliveryCenterStaffMpt VO Assembler
 *
 * @author VSO Team
 */
@Mapper
public interface DeliveryCenterStaffMptAssembler {

    DeliveryCenterStaffMptAssembler INSTANCE = Mappers.getMapper(DeliveryCenterStaffMptAssembler.class);

    DeliveryCenterStaffMpt toVo(DeliveryStaffResult result);

    List<DeliveryCenterStaffMpt> toVoList(List<DeliveryStaffResult> result);

}
