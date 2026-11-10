package net.hwyz.iov.cloud.otd.vso.service.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.hwyz.iov.cloud.otd.vso.api.enums.PaymentStatus;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.PaymentConflictException;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.PaymentStatusMismatchException;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付领域对象
 * <p>
 * 承载支付单状态、活动性与终态幂等/冲突判定；不依赖持久化实现。
 *
 * @author VSO Team
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    /** 支付业务 ID */
    private String paymentId;

    /** 支付单号 */
    private String paymentNo;

    /** 订单业务 ID */
    private String orderId;

    /** 支付阶段 */
    private String paymentStage;

    /** 支付渠道 */
    private String paymentChannel;

    /** 支付金额 */
    private BigDecimal paymentAmount;

    /** 支付状态 */
    private PaymentStatus paymentStatus;

    /** 发起角色 */
    private String initiatorRole;

    /** 发起人 */
    private String initiatorId;

    /** 外部交易单号 */
    private String externalTradeNo;

    /** 支付成功时间 */
    private LocalDateTime payTime;

    /** 待支付截止时间 */
    private LocalDateTime expireTime;

    /** 失败原因 */
    private String failReason;

    /**
     * 是否为活动支付单：PENDING_PAYMENT（未过期）或 PROCESSING
     */
    public boolean isActive(LocalDateTime now) {
        if (paymentStatus == PaymentStatus.PROCESSING) {
            return true;
        }
        return paymentStatus == PaymentStatus.PENDING_PAYMENT && !isExpired(now);
    }

    /**
     * 是否已过期
     */
    public boolean isExpired(LocalDateTime now) {
        return expireTime != null && now.isAfter(expireTime);
    }

    /**
     * 是否为终态（PAID / PAYMENT_FAILED / CANCELLED / CLOSED）
     */
    public boolean isTerminal() {
        return paymentStatus == PaymentStatus.PAID
                || paymentStatus == PaymentStatus.PAYMENT_FAILED
                || paymentStatus == PaymentStatus.CANCELLED
                || paymentStatus == PaymentStatus.CLOSED;
    }

    /**
     * 关闭过期的待支付支付单
     */
    public void closeExpired() {
        if (paymentStatus == PaymentStatus.PENDING_PAYMENT) {
            paymentStatus = PaymentStatus.CLOSED;
            failReason = "支付单已过期关闭";
        }
    }

    /**
     * 标记处理中
     */
    public void markProcessing(String externalTradeNo) {
        if (paymentStatus == PaymentStatus.PENDING_PAYMENT || paymentStatus == PaymentStatus.PROCESSING) {
            paymentStatus = PaymentStatus.PROCESSING;
            if (externalTradeNo != null) {
                this.externalTradeNo = externalTradeNo;
            }
        }
    }

    /**
     * 标记支付成功；重复成功幂等，终态冲突拒绝覆盖
     */
    public void markPaid(String externalTradeNo, LocalDateTime payTime) {
        if (paymentStatus == PaymentStatus.PAID) {
            this.externalTradeNo = externalTradeNo != null ? externalTradeNo : this.externalTradeNo;
            this.payTime = payTime != null ? payTime : this.payTime;
            return;
        }
        if (isTerminal()) {
            throw new PaymentConflictException(
                    "支付单[" + paymentNo + "]已处于终态[" + paymentStatus + "]，不允许变更");
        }
        this.paymentStatus = PaymentStatus.PAID;
        this.externalTradeNo = externalTradeNo;
        this.payTime = payTime != null ? payTime : LocalDateTime.now();
        this.failReason = null;
    }

    /**
     * 标记支付失败；已成功或已取消时视为冲突
     */
    public void markFailed(String failReason) {
        if (paymentStatus == PaymentStatus.PAID || paymentStatus == PaymentStatus.CANCELLED) {
            throw new PaymentConflictException(
                    "支付单[" + paymentNo + "]已处于终态[" + paymentStatus + "]，不允许变更为失败");
        }
        this.paymentStatus = PaymentStatus.PAYMENT_FAILED;
        this.failReason = failReason;
    }

    /**
     * 标记支付取消
     */
    public void markCancelled(String reason) {
        if (paymentStatus == PaymentStatus.PAID) {
            throw new PaymentConflictException(
                    "支付单[" + paymentNo + "]已支付成功，不允许取消");
        }
        this.paymentStatus = PaymentStatus.CANCELLED;
        this.failReason = reason;
    }

    /**
     * 校验支付单可用于支付发起
     */
    public void assertPayable() {
        if (isTerminal() && paymentStatus != PaymentStatus.PENDING_PAYMENT) {
            throw new PaymentStatusMismatchException("支付单[" + paymentNo + "]当前状态[" + paymentStatus + "]不允许操作");
        }
    }
}
