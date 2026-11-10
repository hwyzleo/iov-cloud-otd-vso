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
    @Mapping(target = "modelCode", source = "vo.modelCode")
    @Mapping(target = "variantCode", source = "vo.variantCode")
    @Mapping(target = "optionCodes", source = "vo.optionCodes")
    @Mapping(target = "agreementConfirmations", defaultExpression = "java(toAgreementCmds(vo))")
    EarnestMoneyCmd toCmd(String accountId, EarnestMoneyOrderRequestVo vo);

    /**
     * 协议确认 VO → Cmd（显式映射，禁止忽略）
     */
    default java.util.List<net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.AgreementConfirmationCmd> toAgreementCmds(
            EarnestMoneyOrderRequestVo vo) {
        if (vo == null || vo.getAgreementConfirmations() == null) {
            return null;
        }
        return vo.getAgreementConfirmations().stream()
                .map(item -> net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.AgreementConfirmationCmd.builder()
                        .agreementCode(item.getAgreementCode())
                        .versionNo(item.getVersionNo())
                        .build())
                .collect(java.util.stream.Collectors.toList());
    }

}
