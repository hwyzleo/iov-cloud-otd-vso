package net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * 删除心愿单请求 Vo
 *
 * @author VSO Team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeleteWishlistRequestVo {

    @NotBlank(message = "心愿单ID不能为空")
    private String wishlistId;

}