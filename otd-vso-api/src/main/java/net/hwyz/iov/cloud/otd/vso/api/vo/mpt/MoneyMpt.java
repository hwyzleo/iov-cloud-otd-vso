package net.hwyz.iov.cloud.otd.vso.api.vo.mpt;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 管理后台金额值对象。
 *
 * @author VSO Team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MoneyMpt {

    /** 金额 */
    private BigDecimal amount;

    /** 币种 */
    private String currency;
}
