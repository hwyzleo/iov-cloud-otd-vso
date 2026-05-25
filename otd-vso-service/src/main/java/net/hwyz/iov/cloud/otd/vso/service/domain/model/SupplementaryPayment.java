package net.hwyz.iov.cloud.otd.vso.service.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.hwyz.iov.cloud.otd.vso.api.enums.SupplementaryPaymentScene;
import net.hwyz.iov.cloud.otd.vso.api.enums.SupplementaryPaymentStatus;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.shared.Money;

import java.time.LocalDateTime;

/**
 * 改配补款领域对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplementaryPayment {

    private Long id;
    private String supplementaryNo;
    private String orderId;
    private Money supplementaryAmount;
    private SupplementaryPaymentStatus supplementaryStatus;
    private SupplementaryPaymentScene supplementaryScene;
    private Integer configVersionNo;
    private String paymentId;
    private LocalDateTime expireTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    /**
     * 检查补款任务是否已过期
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expireTime);
    }

    /**
     * 检查补款任务是否可以支付
     */
    public boolean canPay() {
        return supplementaryStatus == SupplementaryPaymentStatus.PENDING && !isExpired();
    }

    /**
     * 标记为已完成
     */
    public void complete(String paymentId) {
        this.supplementaryStatus = SupplementaryPaymentStatus.COMPLETED;
        this.paymentId = paymentId;
    }

    /**
     * 标记为已取消
     */
    public void cancel() {
        this.supplementaryStatus = SupplementaryPaymentStatus.CANCELLED;
    }

    /**
     * 标记为已过期
     */
    public void expire() {
        this.supplementaryStatus = SupplementaryPaymentStatus.EXPIRED;
    }
}
