package net.hwyz.iov.cloud.otd.vso.service.adapter.web.assembler;

import net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo.OrderPaymentRequestVo;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.PayCmd;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * OrderPaymentRequestVo Assembler
 *
 * @author VSO Team
 */
@Mapper
public interface OrderPaymentRequestVoAssembler {

    OrderPaymentRequestVoAssembler INSTANCE = Mappers.getMapper(OrderPaymentRequestVoAssembler.class);

    @Mapping(target = "accountId", source = "accountId")
    PayCmd toCmd(String accountId, OrderPaymentRequestVo vo);

}
