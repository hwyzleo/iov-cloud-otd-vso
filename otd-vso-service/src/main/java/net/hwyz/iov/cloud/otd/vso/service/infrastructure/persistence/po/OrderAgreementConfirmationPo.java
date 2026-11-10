package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 订单协议确认快照表持久化对象
 *
 * @author VSO Team
 */
@Data
@TableName("vso_order_agreement_confirmation")
public class OrderAgreementConfirmationPo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("confirmation_id")
    private String confirmationId;

    @TableField("order_id")
    private String orderId;

    @TableField("agreement_id")
    private String agreementId;

    @TableField("agreement_code")
    private String agreementCode;

    @TableField("version_no")
    private Integer versionNo;

    @TableField("title_snapshot")
    private String titleSnapshot;

    @TableField("intro_snapshot")
    private String introSnapshot;

    @TableField("detail_snapshot")
    private String detailSnapshot;

    @TableField("confirm_time")
    private LocalDateTime confirmTime;

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
