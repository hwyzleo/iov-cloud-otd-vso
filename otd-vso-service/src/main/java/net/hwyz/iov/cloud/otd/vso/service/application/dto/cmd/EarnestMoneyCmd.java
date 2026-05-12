package net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd;

import lombok.Builder;
import lombok.Data;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderModelConfig;

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
    private String regionCode;
    private String licenseCityCode;
    private String buildConfigCode;
    private Map<String, String> featureConfig;
    private Map<String, OrderModelConfig> modelConfigMap;
    private String wishlistId;

}
