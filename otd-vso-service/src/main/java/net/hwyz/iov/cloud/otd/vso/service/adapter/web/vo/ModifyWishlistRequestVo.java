package net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.Map;

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

    @NotEmpty(message = "特征配置不能为空")
    private Map<String, String> featureConfig;

}