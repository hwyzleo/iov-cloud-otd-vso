package net.hwyz.iov.cloud.otd.vso.service.adapter.web.assembler;

import net.hwyz.iov.cloud.otd.vso.api.vo.response.OrderDetailResponse;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.OrderDetailResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * OrderDetailResponse VO Assembler
 *
 * @author VSO Team
 */
@Mapper
public interface OrderDetailResponseAssembler {

    OrderDetailResponseAssembler INSTANCE = Mappers.getMapper(OrderDetailResponseAssembler.class);

    @Mapping(target = "orderId", source = "orderId")
    @Mapping(target = "orderNo", source = "orderNo")
    OrderDetailResponse toVo(OrderDetailResult result);

}
