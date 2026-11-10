package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.converter;

import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderAgreementConfirmation;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.PurchaseAgreement;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.PurchaseAgreementStatus;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderAgreementConfirmationPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.PurchaseAgreementPo;

/**
 * 购车协议对象转换器（Domain ↔ PO）
 *
 * @author VSO Team
 */
public final class PurchaseAgreementPoConverter {

    private PurchaseAgreementPoConverter() {
    }

    public static PurchaseAgreement toDomain(PurchaseAgreementPo po) {
        if (po == null) {
            return null;
        }
        return PurchaseAgreement.builder()
                .agreementId(po.getAgreementId())
                .agreementCode(po.getAgreementCode())
                .saleModelCode(po.getSaleModelCode())
                .versionNo(po.getVersionNo())
                .title(po.getTitle())
                .intro(po.getIntro())
                .detail(po.getDetail())
                .agreementStatus(PurchaseAgreementStatus.fromValue(po.getAgreementStatus()))
                .effectiveFrom(po.getEffectiveFrom())
                .effectiveTo(po.getEffectiveTo())
                .build();
    }

    public static PurchaseAgreementPo fromDomain(PurchaseAgreement agreement, Long id) {
        PurchaseAgreementPo po = new PurchaseAgreementPo();
        po.setId(id);
        po.setAgreementId(agreement.getAgreementId());
        po.setAgreementCode(agreement.getAgreementCode());
        po.setSaleModelCode(agreement.getSaleModelCode());
        po.setVersionNo(agreement.getVersionNo());
        po.setTitle(agreement.getTitle());
        po.setIntro(agreement.getIntro());
        po.setDetail(agreement.getDetail());
        po.setAgreementStatus(agreement.getAgreementStatus() == null ? null : agreement.getAgreementStatus().getValue());
        po.setEffectiveFrom(agreement.getEffectiveFrom());
        po.setEffectiveTo(agreement.getEffectiveTo());
        return po;
    }

    public static OrderAgreementConfirmationPo fromDomain(OrderAgreementConfirmation confirmation) {
        OrderAgreementConfirmationPo po = new OrderAgreementConfirmationPo();
        po.setConfirmationId(confirmation.getConfirmationId());
        po.setOrderId(confirmation.getOrderId());
        po.setAgreementId(confirmation.getAgreementId());
        po.setAgreementCode(confirmation.getAgreementCode());
        po.setVersionNo(confirmation.getVersionNo());
        po.setTitleSnapshot(confirmation.getTitleSnapshot());
        po.setIntroSnapshot(confirmation.getIntroSnapshot());
        po.setDetailSnapshot(confirmation.getDetailSnapshot());
        po.setConfirmTime(confirmation.getConfirmTime());
        return po;
    }

    public static OrderAgreementConfirmation toDomain(OrderAgreementConfirmationPo po) {
        if (po == null) {
            return null;
        }
        return OrderAgreementConfirmation.builder()
                .confirmationId(po.getConfirmationId())
                .orderId(po.getOrderId())
                .agreementId(po.getAgreementId())
                .agreementCode(po.getAgreementCode())
                .versionNo(po.getVersionNo())
                .titleSnapshot(po.getTitleSnapshot())
                .introSnapshot(po.getIntroSnapshot())
                .detailSnapshot(po.getDetailSnapshot())
                .confirmTime(po.getConfirmTime())
                .build();
    }
}
