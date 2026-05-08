package net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * 创建心愿单请求 Vo
 *
 * @author VSO Team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateWishlistRequestVo {

    @NotBlank(message = "销售代码不能为空")
    private String saleCode;

    private String buildConfigCode;

}