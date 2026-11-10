package net.hwyz.iov.cloud.otd.vso.service.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 订单协议确认快照领域对象
 *
 * @author VSO Team
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderAgreementConfirmation {

    private String confirmationId;

    private String orderId;

    private String agreementId;

    private String agreementCode;

    private Integer versionNo;

    private String titleSnapshot;

    private String introSnapshot;

    private String detailSnapshot;

    private LocalDateTime confirmTime;

    /**
     * 从协议模板生成确认快照
     */
    public static OrderAgreementConfirmation snapshotOf(String confirmationId, String orderId,
                                                        PurchaseAgreement agreement, LocalDateTime confirmTime) {
        return OrderAgreementConfirmation.builder()
                .confirmationId(confirmationId)
                .orderId(orderId)
                .agreementId(agreement.getAgreementId())
                .agreementCode(agreement.getAgreementCode())
                .versionNo(agreement.getVersionNo())
                .titleSnapshot(agreement.getTitle())
                .introSnapshot(agreement.getIntro())
                .detailSnapshot(agreement.getDetail())
                .confirmTime(confirmTime)
                .build();
    }
}
