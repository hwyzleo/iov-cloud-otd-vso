package net.hwyz.iov.cloud.otd.vso.service.adapter.web.assembler;

import net.hwyz.iov.cloud.otd.vso.api.vo.request.Order;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.*;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * Service Order Request Assembler
 *
 * @author VSO Team
 */
@Mapper
public interface ServiceOrderRequestAssembler {

    ServiceOrderRequestAssembler INSTANCE = Mappers.getMapper(ServiceOrderRequestAssembler.class);

    PrepareTransportCmd toPrepareTransportCmd(Order order);

    TransportingCmd toTransportingCmd(Order order);

    PrepareDeliveryCmd toPrepareDeliveryCmd(Order order);

    DeliveredCmd toDeliveredCmd(Order order);

    ActivateCmd toActivateCmd(Order order);

}
