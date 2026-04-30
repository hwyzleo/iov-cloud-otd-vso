package net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd;

import lombok.Builder;
import lombok.Data;

/**
 * 定金订单命令（废弃，保留兼容性）
 *
 * @author VSO Team
 */
@Deprecated
@Data
@Builder
public class DownPaymentOrderCmd {

    private String accountId;
    private String orderNum;
    private String saleCode;

}
