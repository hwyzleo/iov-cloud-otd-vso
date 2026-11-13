package net.hwyz.iov.cloud.otd.vso.api.vo.mpt;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 管理后台订单选装明细快照。
 *
 * @author VSO Team
 */
@Data
public class OptionBreakdownMpt {

    private String optionCode;
    private String optionFamilyCode;
    private String optionFamilyName;
    private String optionName;
    private BigDecimal optionPrice;
}
