package net.hwyz.iov.cloud.otd.vso.service.domain.repository;

import net.hwyz.iov.cloud.otd.vso.service.domain.model.PurchaseAgreement;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 购车协议模板仓储接口
 *
 * @author VSO Team
 */
public interface PurchaseAgreementRepository {

    PurchaseAgreement save(PurchaseAgreement agreement);

    Optional<PurchaseAgreement> findById(Long id);

    Optional<PurchaseAgreement> findByAgreementId(String agreementId);

    /**
     * 查询车型（含 GLOBAL 回退）的当前生效协议，同代码取最高版本、车型专属优先
     */
    List<PurchaseAgreement> findActiveBySaleModel(String saleModelCode, LocalDateTime now);

    /**
     * 查询指定代码（含 GLOBAL 回退）的生效协议
     */
    List<PurchaseAgreement> findActiveByCode(String saleModelCode, String agreementCode,
                                             Integer versionNo, LocalDateTime now);

    /**
     * 后台管理查询
     */
    List<PurchaseAgreement> search(String saleModelCode, String agreementCode, String agreementStatus);

    /**
     * 逻辑删除
     */
    void delete(Long id);
}
