package net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd;

import lombok.Builder;
import lombok.Data;

/**
 * 删除心愿单命令
 *
 * @author VSO Team
 */
@Data
@Builder
public class DeleteWishlistCmd {

    private String accountId;
    private String wishlistId;

}
