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

    /** 发运申请人 */
    private String operatorId;

    /** 发运申请人姓名 */
    private String operatorName;

}
