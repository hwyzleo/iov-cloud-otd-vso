package net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd;

import lombok.Builder;
import lombok.Data;

/**
 * 订单ID命令
 *
 * @author VSO Team
 */
@Data
@Builder
public class OrderIdCmd {

    private String orderNo;

}
