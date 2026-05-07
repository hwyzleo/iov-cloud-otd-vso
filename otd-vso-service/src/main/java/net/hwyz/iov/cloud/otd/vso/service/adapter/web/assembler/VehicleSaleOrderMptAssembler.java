package net.hwyz.iov.cloud.otd.vso.service.adapter.web.assembler;

import net.hwyz.iov.cloud.otd.vso.api.vo.mpt.VehicleSaleOrderMpt;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.OrderDetailResult;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.OrderListResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * VehicleSaleOrderMpt VO Assembler
 *
 * @author VSO Team
 */
@Mapper
public interface VehicleSaleOrderMptAssembler {

    VehicleSaleOrderMptAssembler INSTANCE = Mappers.getMapper(VehicleSaleOrderMptAssembler.class);

    @Mapping(target = "orderNo", source = "orderNo")
    VehicleSaleOrderMpt toVo(OrderListResult result);

    List<VehicleSaleOrderMpt> toVoList(List<OrderListResult> result);

    @Mapping(target = "orderNo", source = "orderNo")
    VehicleSaleOrderMpt toVo(OrderDetailResult result);

}
