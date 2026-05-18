package net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.Map;

/**
 * 修改订单配置请求 Vo
 *
 * @author VSO Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModifyOrderConfigRequestVo {

    /**
     * 订单号
     */
    @NotBlank(message = "订单号不能为空")
    private String orderNo;

    /**
     * 销售车型配置类型（特征值选择）
     * key: 特征族编码
     * value: 特征值编码
     */
    @NotEmpty(message = "特征配置不能为空")
    private Map<String, String> saleModelConfigType;

}