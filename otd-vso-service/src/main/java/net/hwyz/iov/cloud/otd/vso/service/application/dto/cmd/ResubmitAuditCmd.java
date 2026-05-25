package net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResubmitAuditCmd {

    private String orderId;
    private String operatorId;
}
