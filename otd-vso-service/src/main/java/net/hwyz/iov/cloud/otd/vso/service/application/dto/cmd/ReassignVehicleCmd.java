package net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd;

import lombok.Builder;
import lombok.Data;

/**
 * 换绑VIN命令
 */
@Data
@Builder
public class ReassignVehicleCmd {

    private String orderNo;
    private String newVin;
    private String operatorId;

}