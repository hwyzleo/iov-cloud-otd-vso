package net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd;

import lombok.Builder;
import lombok.Data;

/**
 * 支付命令
 *
 * @author VSO Team
 */
@Data
@Builder
public class PayCmd {

    private String accountId;
    private String orderNum;
    private java.math.BigDecimal paymentAmount;

}
