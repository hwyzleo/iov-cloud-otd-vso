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

    @Mapping(target = "dealershipCode", source = "orderStoreCode")
    @Mapping(target = "dealershipName", source = "orderStoreName")
    @Mapping(target = "deliveryCenterCode", source = "deliveryStoreCode")
    @Mapping(target = "deliveryCenterName", source = "deliveryStoreName")
    OrderResponseVo toVo(OrderDetailResult result);

    @Mapping(target = "accountId", source = "accountId")
    RequestRefundCmd toRequestRefundCmd(String accountId, OrderVo vo);

}
