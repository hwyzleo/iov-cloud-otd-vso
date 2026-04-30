package net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd;

import lombok.Builder;
import lombok.Data;

/**
 * 申请退款命令
 *
 * @author VSO Team
 */
@Data
@Builder
public class RequestRefundCmd {

    private String accountId;
    private String orderNum;

}
