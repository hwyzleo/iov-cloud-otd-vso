package net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.*;
import lombok.experimental.SuperBuilder;
import net.hwyz.iov.cloud.framework.mysql.po.BasePo;

/**
 * <p>
 * 购车权益 数据对象
 * </p>
 *
 * @author hwyz_leo
 * @since 2024-10-14
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@TableName("tb_purchase_benefits")
public class PurchaseBenefitsPo extends BasePo {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 销售代码
     */
    @TableField("sale_code")
    private String saleCode;

    /**
     * 权益开始时间
     */
    @TableField("start_time")
    private Date startTime;

    /**
     * 权益结束时间
     */
    @TableField("end_time")
    private Date endTime;

    /**
     * 权益简介
     */
    @TableField("intro")
    private String intro;

    /**
     * 权益详情
     */
    @TableField("detail")
    private String detail;

    /**
     * 是否启用
     */
    @TableField("enable")
    private Boolean enable;
}
