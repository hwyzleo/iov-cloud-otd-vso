package net.hwyz.iov.cloud.otd.vso.service.application.dto.query;

import lombok.Builder;
import lombok.Data;

/**
 * 账号ID查询参数
 *
 * @author VSO Team
 */
@Data
@Builder
public class AccountIdQuery {

    private String accountId;
    private String orderNo;

}
