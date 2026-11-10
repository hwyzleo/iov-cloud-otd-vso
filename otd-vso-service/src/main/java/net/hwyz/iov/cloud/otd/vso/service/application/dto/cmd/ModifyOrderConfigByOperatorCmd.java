package net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * MPT 运营修改订单配置命令（改配提交）
 *
 * @author VSO Team
 */
@Data
@Builder
public class ModifyOrderConfigByOperatorCmd {

    /**
     * 订单业务 ID
     */
    private String orderId;

    /**
     * 操作人（安全上下文）
     */
    private String operatorId;

    /**
     * 目标 Variant 编码（为空表示仅调整选装）
     */
    private String variantCode;

    /**
     * 目标 Option 编码列表
     */
    private List<String> optionCodes;

    /**
     * 改配原因（运营填写）
     */
    private String reason;
}
