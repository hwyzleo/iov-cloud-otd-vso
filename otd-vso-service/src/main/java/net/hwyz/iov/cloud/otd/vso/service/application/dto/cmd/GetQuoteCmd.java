package net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GetQuoteCmd {
    /**
     * 销售车型编码
     */
    private String saleModelCode;

    /**
     * OptionCode 列表
     */
    private List<String> optionCodes;

    /**
     * 区域编码
     */
    private String regionCode;
}
