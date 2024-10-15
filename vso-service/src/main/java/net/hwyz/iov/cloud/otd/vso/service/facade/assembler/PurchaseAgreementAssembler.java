package net.hwyz.iov.cloud.otd.vso.service.facade.assembler;

import net.hwyz.iov.cloud.otd.vso.api.contract.PurchaseAgreement;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.po.PurchaseAgreementPo;
import org.mapstruct.Mapper;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

/**
 * 销售车型购车协议转换类
 *
 * @author hwyz_leo
 */
@Mapper
public interface PurchaseAgreementAssembler {

    PurchaseAgreementAssembler INSTANCE = Mappers.getMapper(PurchaseAgreementAssembler.class);

    /**
     * 数据对象转数据传输对象
     *
     * @param purchaseAgreementPo 数据对象
     * @return 数据传输对象
     */
    @Mappings({})
    PurchaseAgreement fromPo(PurchaseAgreementPo purchaseAgreementPo);

}
