package net.hwyz.iov.cloud.otd.vso.service.adapter.web.assembler;

import net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo.EarnestMoneyOrderRequestVo;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.EarnestMoneyCmd;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * EarnestMoneyOrderRequestVo Assembler
 *
 * @author VSO Team
 */
@Mapper
public interface EarnestMoneyOrderRequestVoAssembler {

    EarnestMoneyOrderRequestVoAssembler INSTANCE = Mappers.getMapper(EarnestMoneyOrderRequestVoAssembler.class);

    @Mapping(target = "accountId", source = "accountId")
    @Mapping(target = "saleModel", source = "vo.saleModelCode")
    @Mapping(target = "featureConfig", source = "vo.saleModelConfigType")
    @Mapping(target = "regionCode", source = "vo.regionCode")
    EarnestMoneyCmd toCmd(String accountId, EarnestMoneyOrderRequestVo vo);

}
