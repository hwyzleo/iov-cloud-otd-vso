package net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 修改心愿单命令
 *
 * @author VSO Team
 */
@Data
@Builder
public class ModifyWishlistCmd {

    private String accountId;
    private String wishlistId;
    private String configurationCode;
    private List<String> optionCodes;

}
