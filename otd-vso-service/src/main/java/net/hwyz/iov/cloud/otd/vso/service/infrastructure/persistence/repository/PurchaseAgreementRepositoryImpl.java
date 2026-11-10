package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.repository;

import com.github.pagehelper.Page;
import lombok.RequiredArgsConstructor;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.PurchaseAgreement;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.PurchaseAgreementRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.converter.PurchaseAgreementPoConverter;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.PurchaseAgreementMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.PurchaseAgreementPo;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 购车协议模板仓储实现
 *
 * @author VSO Team
 */
@Repository
@RequiredArgsConstructor
public class PurchaseAgreementRepositoryImpl implements PurchaseAgreementRepository {

    private final PurchaseAgreementMapper purchaseAgreementMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PurchaseAgreement save(PurchaseAgreement agreement) {
        PurchaseAgreementPo existing = purchaseAgreementMapper.selectByAgreementId(agreement.getAgreementId());
        PurchaseAgreementPo po = PurchaseAgreementPoConverter.fromDomain(agreement,
                existing == null ? null : existing.getId());
        if (existing == null) {
            purchaseAgreementMapper.insertPo(po);
        } else {
            purchaseAgreementMapper.updatePo(po);
        }
        return agreement;
    }

    @Override
    public Optional<PurchaseAgreement> findById(Long id) {
        return Optional.ofNullable(purchaseAgreementMapper.selectPoById(id))
                .map(PurchaseAgreementPoConverter::toDomain);
    }

    @Override
    public Optional<PurchaseAgreement> findByAgreementId(String agreementId) {
        return Optional.ofNullable(purchaseAgreementMapper.selectByAgreementId(agreementId))
                .map(PurchaseAgreementPoConverter::toDomain);
    }

    @Override
    public List<PurchaseAgreement> findActiveBySaleModel(String saleModelCode, LocalDateTime now) {
        return purchaseAgreementMapper.selectActiveBySaleModel(saleModelCode, now).stream()
                .map(PurchaseAgreementPoConverter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<PurchaseAgreement> findActiveByCode(String saleModelCode, String agreementCode,
                                                    Integer versionNo, LocalDateTime now) {
        return purchaseAgreementMapper.selectActiveByCode(saleModelCode, agreementCode, versionNo, now).stream()
                .map(PurchaseAgreementPoConverter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<PurchaseAgreement> search(String saleModelCode, String agreementCode, String agreementStatus) {
        Map<String, Object> params = new HashMap<>();
        if (saleModelCode != null && !saleModelCode.isEmpty()) {
            params.put("saleModelCode", saleModelCode);
        }
        if (agreementCode != null && !agreementCode.isEmpty()) {
            params.put("agreementCode", agreementCode);
        }
        if (agreementStatus != null && !agreementStatus.isEmpty()) {
            params.put("agreementStatus", agreementStatus);
        }
        List<PurchaseAgreementPo> poList = purchaseAgreementMapper.selectPoByMap(params);
        List<PurchaseAgreement> agreements = poList.stream()
                .map(PurchaseAgreementPoConverter::toDomain)
                .collect(Collectors.toList());
        // 保留 PageHelper 分页元信息（total），否则后台列表 total 退化为当前页条数
        if (poList instanceof Page<PurchaseAgreementPo> page) {
            Page<PurchaseAgreement> paged = new Page<>(page.getPageNum(), page.getPageSize(), page.isCount());
            paged.setTotal(page.getTotal());
            paged.addAll(agreements);
            return paged;
        }
        return agreements;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        purchaseAgreementMapper.logicalDeletePo(id);
    }
}
