package net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * 创建心愿单命令
 *
 * @author VSO Team
 */
@Data
@Builder
public class CreateWishlistCmd {

    private String accountId;
    private String saleModelCode;
    private Map<String, String> featureConfig;

}
