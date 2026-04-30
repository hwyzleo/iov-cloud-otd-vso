package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 发票表持久化对象
 */
@Data
@TableName("vso_invoice")
public class InvoicePo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("invoice_id")
    private String invoiceId;

    @TableField("invoice_no")
    private String invoiceNo;

    @TableField("order_id")
    private String orderId;

    @TableField("invoice_status")
    private String invoiceStatus;

    @TableField("invoice_type")
    private String invoiceType;

    @TableField("invoice_title")
    private String invoiceTitle;

    @TableField("invoice_tax_no")
    private String invoiceTaxNo;

    @TableField("invoice_amount")
    private BigDecimal invoiceAmount;

    @TableField("invoice_subject_code")
    private String invoiceSubjectCode;

    @TableField("issue_time")
    private LocalDateTime issueTime;

    @TableField("void_time")
    private LocalDateTime voidTime;

    @TableField("reissue_flag")
    private Integer reissueFlag;

    @TableField("void_reason")
    private String voidReason;

    @TableField("external_invoice_no")
    private String externalInvoiceNo;

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
