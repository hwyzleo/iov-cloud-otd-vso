package net.hwyz.iov.cloud.otd.vso.service.facade.assembler;

import net.hwyz.iov.cloud.otd.vso.api.contract.PurchaseBenefits;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.po.PurchaseBenefitsPo;
import org.mapstruct.Mapper;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

/**
 * 销售车型购车权益转换类
 *
 * @author hwyz_leo
 */
@Mapper
public interface PurchaseBenefitsAssembler {

    PurchaseBenefitsAssembler INSTANCE = Mappers.getMapper(PurchaseBenefitsAssembler.class);

    /**
     * 数据对象转数据传输对象
     *
     * @param purchaseBenefitsPo 数据对象
     * @return 数据传输对象
     */
    @Mappings({})
    PurchaseBenefits fromPo(PurchaseBenefitsPo purchaseBenefitsPo);

}
