package net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd;

import lombok.Builder;
import lombok.Data;

/**
 * 激活车辆命令
 *
 * @author VSO Team
 */
@Data
@Builder
public class ActivateCmd {

    private String orderNo;

}
