package net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import net.hwyz.iov.cloud.tsp.framework.mysql.po.BasePo;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * <p>
 * 车辆销售订单 数据对象
 * </p>
 *
 * @author hwyz_leo
 * @since 2024-10-10
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@TableName("tb_order")
public class OrderPo extends BasePo {

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
     * 订单状态：100-心愿单
     */
    @TableField("order_state")
    private Integer orderState;

    /**
     * 销售代码
     */
    @TableField("sale_code")
    private String saleCode;

    /**
     * 车型配置代码
     */
    @TableField("model_config_code")
    private String modelConfigCode;

    /**
     * 车型配置是否锁定
     */
    @TableField("model_config_lock")
    private Boolean modelConfigLock;

    /**
     * 支付状态
     */
    @TableField("pay_state")
    private Integer payState;

    /**
     * 订单类型：1-C端普通客户，2-C端大客户，3-B端大客户，4-员工内购，5-员工推荐，6-内部用车，7-媒体用车，8-展车用车
     */
    @TableField("order_type")
    private Integer orderType;

    /**
     * 下单人员ID
     */
    @TableField("order_person_id")
    private String orderPersonId;

    /**
     * 下单人员姓名
     */
    @TableField("order_person_name")
    private String orderPersonName;

    /**
     * 下单人员电话
     */
    @TableField("order_person_phone")
    private String orderPersonPhone;

    /**
     * 推荐用户ID
     */
    @TableField("recommender_id")
    private String recommenderId;

    /**
     * 联系人姓名
     */
    @TableField("contact_person_name")
    private String contactPersonName;

    /**
     * 联系人电话
     */
    @TableField("contact_person_phone")
    private String contactPersonPhone;

    /**
     * 销售人员ID
     */
    @TableField("sales_person_id")
    private String salesPersonId;

    /**
     * 销售人员姓名
     */
    @TableField("sales_person_name")
    private String salesPersonName;

    /**
     * 销售人员电话
     */
    @TableField("sales_person_phone")
    private String salesPersonPhone;

    /**
     * 销售人员是否代理下单
     */
    @TableField("sales_person_order")
    private Byte salesPersonOrder;

    /**
     * 销售渠道
     */
    @TableField("sales_channel")
    private String salesChannel;

    /**
     * 购车方案：1-全款购车，2-金融贷款
     */
    @TableField("buy_plan")
    private String buyPlan;

    /**
     * 交付城市
     */
    @TableField("delivery_city")
    private String deliveryCity;

    /**
     * 交付方式：1-现场交付，2-远程交付，3-钣喷交付
     */
    @TableField("delivery_method")
    private String deliveryMethod;

    /**
     * 交付人员ID
     */
    @TableField("delivery_person_id")
    private String deliveryPersonId;

    /**
     * 交付人员姓名
     */
    @TableField("delivery_person_name")
    private String deliveryPersonName;

    /**
     * 交付备注
     */
    @TableField("delivery_remark")
    private String deliveryRemark;

    /**
     * 接待人员ID
     */
    @TableField("reception_person_id")
    private String receptionPersonId;

    /**
     * 接待人员姓名
     */
    @TableField("reception_person_name")
    private String receptionPersonName;

    /**
     * 开票时间
     */
    @TableField("invoicing_time")
    private Date invoicingTime;

    /**
     * 预计交付时间
     */
    @TableField("estimated_delivery_time")
    private Date estimatedDeliveryTime;

    /**
     * 实际交付时间
     */
    @TableField("actual_delivery_time")
    private Date actualDeliveryTime;

    /**
     * 上牌城市
     */
    @TableField("license_city")
    private String licenseCity;
}
