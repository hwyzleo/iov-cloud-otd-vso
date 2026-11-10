package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.PurchaseAgreementPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 购车协议模板 Mapper 接口
 *
 * @author VSO Team
 */
@Mapper
public interface PurchaseAgreementMapper {

    int insertPo(PurchaseAgreementPo po);

    int updatePo(PurchaseAgreementPo po);

    int logicalDeletePo(@Param("id") Long id);

    PurchaseAgreementPo selectPoById(@Param("id") Long id);

    PurchaseAgreementPo selectByAgreementId(@Param("agreementId") String agreementId);

    List<PurchaseAgreementPo> selectPoByMap(@Param("params") Map<String, Object> params);

    /**
     * 查询指定车型（含 GLOBAL 回退）的生效协议
     */
    List<PurchaseAgreementPo> selectActiveBySaleModel(@Param("saleModelCode") String saleModelCode,
                                                      @Param("now") LocalDateTime now);

    /**
     * 查询指定车型（含 GLOBAL 回退）指定代码的生效协议，按车型专属优先、版本降序
     */
    List<PurchaseAgreementPo> selectActiveByCode(@Param("saleModelCode") String saleModelCode,
                                                 @Param("agreementCode") String agreementCode,
                                                 @Param("versionNo") Integer versionNo,
                                                 @Param("now") LocalDateTime now);
}
