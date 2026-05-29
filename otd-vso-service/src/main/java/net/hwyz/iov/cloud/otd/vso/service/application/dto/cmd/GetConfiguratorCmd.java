package net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GetConfiguratorCmd {
    /**
     * 销售车型编码
     */
    private String saleModelCode;

    /**
     * 区域编码
     */
    private String regionCode;
}
