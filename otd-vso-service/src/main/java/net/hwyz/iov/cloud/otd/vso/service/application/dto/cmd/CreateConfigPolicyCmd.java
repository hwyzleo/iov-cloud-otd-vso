package net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CreateConfigPolicyCmd {
    /**
     * 销售车型编码
     */
    private String saleModelCode;

    /**
     * Configuration 编码列表
     */
    private List<String> configurationCodes;

    /**
     * 状态，默认 active
     */
    private String status;
}
