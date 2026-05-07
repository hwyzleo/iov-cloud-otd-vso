package net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd;

import lombok.Builder;
import lombok.Data;

/**
 * 取消订单命令
 *
 * @author VSO Team
 */
@Data
@Builder
public class CancelCmd {

    private String accountId;
    private String orderNo;

}
