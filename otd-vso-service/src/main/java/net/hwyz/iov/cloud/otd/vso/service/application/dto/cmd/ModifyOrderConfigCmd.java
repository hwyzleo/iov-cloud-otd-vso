package net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 修改订单配置命令
 *
 * @author VSO Team
 */
@Data
@Builder
public class ModifyOrderConfigCmd {

    private String accountId;
    private String orderNo;
    private List<String> optionCodes;

}
