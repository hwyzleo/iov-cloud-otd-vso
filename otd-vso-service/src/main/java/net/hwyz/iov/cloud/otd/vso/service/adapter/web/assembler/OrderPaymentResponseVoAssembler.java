package net.hwyz.iov.cloud.otd.vso.service.adapter.web.assembler;

import net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo.OrderPaymentResponseVo;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.PayResult;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * OrderPaymentResponseVo Assembler
 *
 * @author VSO Team
 */
@Mapper
public interface OrderPaymentResponseVoAssembler {

    OrderPaymentResponseVoAssembler INSTANCE = Mappers.getMapper(OrderPaymentResponseVoAssembler.class);

    OrderPaymentResponseVo toVo(PayResult result);

}
