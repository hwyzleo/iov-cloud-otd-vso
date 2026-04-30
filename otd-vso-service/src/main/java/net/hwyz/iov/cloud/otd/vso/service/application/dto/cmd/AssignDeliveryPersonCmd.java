package net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd;

import lombok.Builder;
import lombok.Data;

/**
 * 分配交付人员命令
 *
 * @author VSO Team
 */
@Data
@Builder
public class AssignDeliveryPersonCmd {

    private String orderNum;
    private String deliveryPersonId;
    private String deliveryPersonName;

}
