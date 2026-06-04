package net.hwyz.iov.cloud.otd.vso.service.adapter.web.assembler;

import net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo.OrderResponseVo;
import net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo.OrderVo;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.RequestRefundCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.OrderDetailResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * OrderResponseVo Assembler
 *
 * @author VSO Team
 */
@Mapper
public interface OrderResponseVoAssembler {

    OrderResponseVoAssembler INSTANCE = Mappers.getMapper(OrderResponseVoAssembler.class);

    @Mapping(target = "modelCode", source = "vehicleInfo.modelCode")
    @Mapping(target = "modelName", source = "vehicleInfo.modelName")
    @Mapping(target = "variantCode", source = "vehicleInfo.variantCode")
    @Mapping(target = "variantName", source = "vehicleInfo.variantName")
    @Mapping(target = "configurationCode", source = "vehicleInfo.configurationCode")
    @Mapping(target = "optionCodes", source = "vehicleInfo.optionCodes")
    @Mapping(target = "optionBreakdown", source = "vehicleInfo.optionBreakdown")
    OrderResponseVo toVo(OrderDetailResult result);

    @Mapping(target = "accountId", source = "accountId")
    RequestRefundCmd toRequestRefundCmd(String accountId, OrderVo vo);

}
