package net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import net.hwyz.iov.cloud.tsp.framework.mysql.po.BasePo;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * <p>
 * 购车协议 数据对象
 * </p>
 *
 * @author hwyz_leo
 * @since 2024-10-15
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@TableName("tb_purchase_agreement")
public class PurchaseAgreementPo extends BasePo {

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
     * 协议类型：1-意向金（小定），2-定金（大定）
     */
    @TableField("type")
    private Integer type;

    /**
     * 协议标题
     */
    @TableField("title")
    private String title;

    /**
     * 协议简介
     */
    @TableField("intro")
    private String intro;

    /**
     * 协议详情
     */
    @TableField("detail")
    private String detail;

    /**
     * 是否启用
     */
    @TableField("enable")
    private Boolean enable;
}
