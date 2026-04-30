package net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd;

import lombok.Builder;
import lombok.Data;

/**
 * 账号ID查询参数
 *
 * @author VSO Team
 */
@Data
@Builder
public class AccountIdCmd {

    private String accountId;
    private String orderNum;

}
