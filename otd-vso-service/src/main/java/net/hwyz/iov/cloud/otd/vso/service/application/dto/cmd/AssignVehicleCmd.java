package net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd;

import lombok.Builder;
import lombok.Data;

/**
 * 分配车辆命令
 *
 * @author VSO Team
 */
@Data
@Builder
public class AssignVehicleCmd {

    private String orderNo;
    private String vin;

}
