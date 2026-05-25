package net.hwyz.iov.cloud.otd.vso.service.domain.model.shared;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 金额值对象
 *
 * @author VSO Team
 */
@Getter
@NoArgsConstructor
public class Money {

    /**
     * 零金额常量（人民币）
     */
    public static final Money ZERO_CNY = new Money(BigDecimal.ZERO, "CNY");

    /**
     * 金额值
     */
    private BigDecimal amount;

    /**
     * 币种
     */
    private String currency;

    public Money(BigDecimal amount) {
        this(amount, "CNY");
    }

    public Money(BigDecimal amount, String currency) {
        if (amount == null) {
            throw new IllegalArgumentException("金额不能为null");
        }
        this.amount = amount;
        this.currency = currency != null ? currency : "CNY";
    }

    /**
     * 创建金额实例
     *
     * @param amount   金额值
     * @param currency 币种
     * @return 金额实例
     */
    public static Money of(BigDecimal amount, String currency) {
        return new Money(amount, currency);
    }

    /**
     * 金额相加
     */
    public Money add(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("币种不同，无法相加");
        }
        return new Money(this.amount.add(other.amount), this.currency);
    }

    /**
     * 金额相减
     */
    public Money subtract(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("币种不同，无法相减");
        }
        return new Money(this.amount.subtract(other.amount), this.currency);
    }

    /**
     * 是否等于零
     */
    public boolean isZero() {
        return this.amount.compareTo(BigDecimal.ZERO) == 0;
    }

    /**
     * 是否大于指定金额
     */
    public boolean isGreaterThan(Money other) {
        return this.amount.compareTo(other.amount) > 0;
    }

    /**
     * 是否小于指定金额
     */
    public boolean isLessThan(Money other) {
        return this.amount.compareTo(other.amount) < 0;
    }

    /**
     * 获取绝对值
     */
    public Money abs() {
        return new Money(this.amount.abs(), this.currency);
    }

    @Override
    public String toString() {
        return amount + " " + currency;
    }

}
