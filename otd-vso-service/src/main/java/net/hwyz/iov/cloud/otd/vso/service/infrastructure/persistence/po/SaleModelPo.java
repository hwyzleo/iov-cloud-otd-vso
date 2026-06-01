package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.sql.Timestamp;
import lombok.*;
import lombok.experimental.SuperBuilder;
import net.hwyz.iov.cloud.framework.mysql.po.BasePo;

/**
 * <p>
 * 销售车型 数据对象
 * </p>
 *
 * @author hwyz_leo
 * @since 2024-10-18
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@TableName("vso_sale_model")
public class SaleModelPo extends BasePo {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 销售车型代码
     */
    @TableField("sale_code")
    private String saleModelCode;

    /**
     * 销售车型名称
     */
    @TableField("model_name")
    private String modelName;

    /**
     * 销售车型相关参数
     */
    @TableField("parameters")
    private String parameters;

    /**
     * 销售车型图片集
     */
    @TableField("images")
    private String images;

    /**
     * 是否允许意向金
     */
    @TableField("earnest_money")
    private Boolean earnestMoney;

    /**
     * 是否允许定金
     */
    @TableField("down_payment")
    private Boolean downPayment;

    /**
     * 是否启用
     */
    @TableField("enable")
    private Boolean enable;

    /**
     * 排序
     */
    @TableField("sort")
    private Integer sort;

    /**
     * MDM Carline 编码（1:1 关联）
     */
    @TableField("carline_code")
    private String carlineCode;

    /**
     * 车型图标 URL
     */
    @TableField("icon")
    private String icon;

    /**
     * 卖点文案
     */
    @TableField("marketing_copy")
    private String marketingCopy;

    /**
     * 上架状态：active/off_shelf
     */
    @TableField("listing_status")
    private String listingStatus;

    /**
     * 上架生效开始时间
     */
    @TableField("effective_from")
    private Timestamp effectiveFrom;

    /**
     * 上架生效结束时间
     */
    @TableField("effective_to")
    private Timestamp effectiveTo;

    /**
     * 可售区域列表，为空表示全国
     */
    @TableField("available_regions")
    private String availableRegions;

    /**
     * 可售渠道列表，为空表示全渠道
     */
    @TableField("channels")
    private String channels;
}
