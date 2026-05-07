package net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd;

import lombok.Builder;
import lombok.Data;

/**
 * 意向金转定金命令
 *
 * @author VSO Team
 */
@Data
@Builder
public class EarnestToDownCmd {

    private String accountId;
    private String orderNo;

}
