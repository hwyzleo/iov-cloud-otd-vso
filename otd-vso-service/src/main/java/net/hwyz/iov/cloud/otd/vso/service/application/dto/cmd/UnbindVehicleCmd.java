package net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd;

import lombok.Builder;
import lombok.Data;

/**
 * 解绑VIN命令
 */
@Data
@Builder
public class UnbindVehicleCmd {

    private String orderNo;
    private String unbindReason;
    private String operatorId;

}