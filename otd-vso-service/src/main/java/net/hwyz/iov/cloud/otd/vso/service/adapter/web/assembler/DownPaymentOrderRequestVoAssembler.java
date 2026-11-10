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
    @Mapping(target = "modelCode", source = "vo.modelCode")
    @Mapping(target = "variantCode", source = "vo.variantCode")
    @Mapping(target = "optionCodes", source = "vo.optionCodes")
    @Mapping(target = "agreementConfirmations", defaultExpression = "java(toAgreementCmds(vo))")
    DownPaymentCmd toCmd(String accountId, DownPaymentOrderRequestVo vo);

    /**
     * 协议确认 VO → Cmd（显式映射，禁止忽略）
     */
    default java.util.List<net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.AgreementConfirmationCmd> toAgreementCmds(
            DownPaymentOrderRequestVo vo) {
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
