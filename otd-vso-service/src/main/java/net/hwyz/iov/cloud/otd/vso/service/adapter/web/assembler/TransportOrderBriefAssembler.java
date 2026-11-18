package net.hwyz.iov.cloud.otd.vso.service.adapter.web.assembler;

import net.hwyz.iov.cloud.otd.vso.api.vo.response.TransportOrderBriefResponse;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.TransportOrderBriefResult;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 待发运订单摘要 Assembler（OTD-VSO-DSN-CR-012）
 *
 * @author VSO Team
 */
@Mapper
public interface TransportOrderBriefAssembler {

    TransportOrderBriefAssembler INSTANCE = Mappers.getMapper(TransportOrderBriefAssembler.class);

    TransportOrderBriefResponse toVo(TransportOrderBriefResult result);
}
