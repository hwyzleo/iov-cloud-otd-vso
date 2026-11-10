package net.hwyz.iov.cloud.otd.vso.api.vo.mpt.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * MPT 运营改配提交请求
 *
 * @author VSO Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModifyOrderConfigByOperatorRequest implements Serializable {

    /**
     * 目标 Variant 编码（为空表示仅调整选装）
     */
    private String variantCode;

    /**
     * 目标 Option 编码列表
     */
    @NotEmpty(message = "OptionCode列表不能为空")
    private List<String> optionCodes;

    /**
     * 改配原因
     */
    @Size(max = 256, message = "改配原因不能超过 256 字符")
    private String reason;
}
