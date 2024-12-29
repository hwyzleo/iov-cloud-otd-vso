package net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import net.hwyz.iov.cloud.framework.mysql.po.BasePo;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * <p>
 * 车辆销售订单日志 数据对象
 * </p>
 *
 * @author hwyz_leo
 * @since 2024-12-29
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@TableName("tb_order_log")
public class OrderLogPo extends BasePo {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 订单编码
     */
    @TableField("order_num")
    private String orderNum;

    /**
     * 操作终端
     */
    @TableField("operate_client")
    private String operateClient;

    /**
     * 操作者ID
     */
    @TableField("account_id")
    private String accountId;

    /**
     * 操作者
     */
    @TableField("operator")
    private String operator;

    /**
     * 操作类型
     */
    @TableField("operate_type")
    private String operateType;

    /**
     * 操作描述
     */
    @TableField("operate_desc")
    private String operateDesc;

    /**
     * 操作时间
     */
    @TableField("operate_time")
    private Date operateTime;
}
