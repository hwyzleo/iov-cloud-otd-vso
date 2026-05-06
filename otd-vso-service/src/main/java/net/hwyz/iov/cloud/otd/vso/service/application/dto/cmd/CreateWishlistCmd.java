package net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd;

import lombok.Builder;
import lombok.Data;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderModelConfig;

import java.util.Map;

/**
 * 创建愿望单命令
 *
 * @author VSO Team
 */
@Data
@Builder
public class CreateWishlistCmd {

    private String accountId;
    private String mobile;
    private String saleCode;
    private String buildConfigCode;
    private Map<String, OrderModelConfig> modelConfigMap;
    private String licenseCityCode;

}
