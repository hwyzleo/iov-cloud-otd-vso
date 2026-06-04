package net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd;

import lombok.Builder;
import lombok.Data;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderModelConfig;

import java.util.List;
import java.util.Map;

/**
 * 意向金订单命令
 *
 * @author VSO Team
 */
@Data
@Builder
public class EarnestMoneyCmd {

    private String accountId;
    private String orderNo;
    private String saleModel;
    private String modelCode;
    private String variantCode;
    private String licenseCityCode;
    private String configurationCode;
    private List<String> optionCodes;
    private Map<String, OrderModelConfig> modelConfigMap;
    private String wishlistId;

}
