package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 购车协议模板表持久化对象
 *
 * @author VSO Team
 */
@Data
@TableName("vso_purchase_agreement")
public class PurchaseAgreementPo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("agreement_id")
    private String agreementId;

    @TableField("agreement_code")
    private String agreementCode;

    @TableField("sale_model_code")
    private String saleModelCode;

    @TableField("version_no")
    private Integer versionNo;

    @TableField("title")
    private String title;

    @TableField("intro")
    private String intro;

    @TableField("detail")
    private String detail;

    @TableField("agreement_status")
    private String agreementStatus;

    @TableField("effective_from")
    private LocalDateTime effectiveFrom;

    @TableField("effective_to")
    private LocalDateTime effectiveTo;

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
