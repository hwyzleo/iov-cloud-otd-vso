package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 外部回调日志表持久化对象
 */
@Data
@TableName("vso_callback_log")
public class CallbackLogPo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("callback_log_id")
    private String callbackLogId;

    @TableField("order_id")
    private String orderId;

    @TableField("business_type")
    private String businessType;

    @TableField("external_system_name")
    private String externalSystemName;

    @TableField("external_business_no")
    private String externalBusinessNo;

    @TableField("idempotent_key")
    private String idempotentKey;

    @TableField("callback_status_value")
    private String callbackStatusValue;

    @TableField("callback_result_code")
    private String callbackResultCode;

    @TableField("event_time")
    private LocalDateTime eventTime;

    @TableField("request_body")
    private String requestBody;

    @TableField("response_body")
    private String responseBody;

    @TableField("process_result")
    private String processResult;

    @TableField("manual_override_flag")
    private Integer manualOverrideFlag;

    @TableField("process_time")
    private LocalDateTime processTime;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private Long createBy;

    @TableField(value = "modify_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime modifyTime;

    @TableField(value = "modify_by", fill = FieldFill.INSERT_UPDATE)
    private Long modifyBy;

    @TableField(value = "row_version", fill = FieldFill.INSERT)
    private Integer rowVersion;

    @TableField(value = "row_valid", fill = FieldFill.INSERT)
    private Integer rowValid;

}
