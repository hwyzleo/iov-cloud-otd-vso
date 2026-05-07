package net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd;

import lombok.Builder;
import lombok.Data;

/**
 * 删除订单命令
 *
 * @author VSO Team
 */
@Data
@Builder
public class RemoveCmd {

    private String orderNo;

}
