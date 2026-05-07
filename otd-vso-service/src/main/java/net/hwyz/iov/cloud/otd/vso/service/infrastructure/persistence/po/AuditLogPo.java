package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统审计日志表持久化对象
 */
@Data
@TableName("vso_audit_log")
public class AuditLogPo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("audit_id")
    private String auditId;

    @TableField("order_id")
    private String orderId;

    @TableField("event_type")
    private String eventType;

    @TableField("event_name")
    private String eventName;

    @TableField("operator_id")
    private String operatorId;

    @TableField("operator_role")
    private String operatorRole;

    @TableField("request_uri")
    private String requestUri;

    @TableField("request_method")
    private String requestMethod;

    @TableField("trace_id")
    private String traceId;

    @TableField("operation_result")
    private String operationResult;

    @TableField("request_snapshot")
    private String requestSnapshot;

    @TableField("response_code")
    private String responseCode;

    @TableField("ip_address")
    private String ipAddress;

    @TableField("event_time")
    private LocalDateTime eventTime;

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
