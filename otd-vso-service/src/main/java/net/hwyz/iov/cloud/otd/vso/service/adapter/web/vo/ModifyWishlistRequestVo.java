package net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * 修改心愿单请求 Vo
 *
 * @author VSO Team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModifyWishlistRequestVo {

    @NotBlank(message = "心愿单ID不能为空")
    private String wishlistId;

    private String buildConfigCode;

}