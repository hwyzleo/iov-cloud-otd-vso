package net.hwyz.iov.cloud.otd.vso.service.adapter.web.assembler;

import net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo.DownPaymentOrderRequestVo;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.DownPaymentCmd;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * DownPaymentOrderRequestVo Assembler
 *
 * @author VSO Team
 */
@Mapper
public interface DownPaymentOrderRequestVoAssembler {

    DownPaymentOrderRequestVoAssembler INSTANCE = Mappers.getMapper(DownPaymentOrderRequestVoAssembler.class);

    @Mapping(target = "accountId", source = "accountId")
    @Mapping(target = "saleModel", source = "vo.saleModelCode")
    @Mapping(target = "featureConfig", source = "vo.saleModelConfigType")
    DownPaymentCmd toCmd(String accountId, DownPaymentOrderRequestVo vo);

}
