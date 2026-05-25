package net.hwyz.iov.cloud.otd.vso.api.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 补款信息响应VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplementaryPaymentVo {

    /**
     * 补款单号
     */
    private String supplementaryNo;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 补款金额
     */
    private BigDecimal supplementaryAmount;

    /**
     * 币种
     */
    private String currency;

    /**
     * 补款状态
     */
    private String supplementaryStatus;

    /**
     * 配置版本号
     */
    private Integer configVersionNo;

    /**
     * 过期时间
     */
    private LocalDateTime expireTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
