package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 合同表持久化对象
 */
@Data
@TableName("vso_contract")
public class ContractPo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("contract_id")
    private String contractId;

    @TableField("order_id")
    private String orderId;

    @TableField("contract_no")
    private String contractNo;

    @TableField("contract_type")
    private String contractType;

    @TableField("version_no")
    private Integer versionNo;

    @TableField("contract_status")
    private String contractStatus;

    @TableField("sign_mode")
    private String signMode;

    @TableField("sign_effective_rule")
    private String signEffectiveRule;

    @TableField("file_url")
    private String fileUrl;

    @TableField("external_contract_no")
    private String externalContractNo;

    @TableField("generate_time")
    private LocalDateTime generateTime;

    @TableField("sign_time")
    private LocalDateTime signTime;

    @TableField("effective_time")
    private LocalDateTime effectiveTime;

    @TableField("invalid_time")
    private LocalDateTime invalidTime;

    @TableField("invalid_reason")
    private String invalidReason;

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
