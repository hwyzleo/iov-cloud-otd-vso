package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 异常单表持久化对象
 */
@Data
@TableName("vso_exception_order")
public class ExceptionOrderPo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("exception_order_id")
    private String exceptionOrderId;

    @TableField("exception_no")
    private String exceptionNo;

    @TableField("order_id")
    private String orderId;

    @TableField("exception_type")
    private String exceptionType;

    @TableField("exception_source")
    private String exceptionSource;

    @TableField("exception_status")
    private String exceptionStatus;

    @TableField("responsible_user_id")
    private String responsibleUserId;

    @TableField("upgrade_flag")
    private Integer upgradeFlag;

    @TableField("exception_desc")
    private String exceptionDesc;

    @TableField("external_system_name")
    private String externalSystemName;

    @TableField("external_error_code")
    private String externalErrorCode;

    @TableField("root_cause")
    private String rootCause;

    @TableField("solution_desc")
    private String solutionDesc;

    @TableField("prevention_desc")
    private String preventionDesc;

    @TableField("discover_time")
    private LocalDateTime discoverTime;

    @TableField("close_time")
    private LocalDateTime closeTime;

    @TableField("close_user_id")
    private String closeUserId;

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
