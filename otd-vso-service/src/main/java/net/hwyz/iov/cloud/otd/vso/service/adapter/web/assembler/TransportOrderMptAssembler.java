package net.hwyz.iov.cloud.otd.vso.service.adapter.web.assembler;

import net.hwyz.iov.cloud.otd.vso.api.vo.mpt.TransportOrderMpt;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.OrderListResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * TransportOrderMpt VO Assembler
 *
 * @author VSO Team
 */
@Mapper
public interface TransportOrderMptAssembler {

    TransportOrderMptAssembler INSTANCE = Mappers.getMapper(TransportOrderMptAssembler.class);

    @Mapping(target = "orderNo", source = "orderNo")
    TransportOrderMpt toVo(OrderListResult result);

    List<TransportOrderMpt> toVoList(List<OrderListResult> result);

}
