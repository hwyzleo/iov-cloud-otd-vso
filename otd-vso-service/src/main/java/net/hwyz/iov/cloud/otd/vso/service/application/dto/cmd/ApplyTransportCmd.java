package net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd;

import lombok.Builder;
import lombok.Data;

/**
 * 申请发运命令
 *
 * @author VSO Team
 */
@Data
@Builder
public class ApplyTransportCmd {

    private String orderNo;

}
