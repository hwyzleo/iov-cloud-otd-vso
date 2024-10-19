package net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import net.hwyz.iov.cloud.tsp.framework.mysql.po.BasePo;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * <p>
 * 订单支付记录 数据对象
 * </p>
 *
 * @author hwyz_leo
 * @since 2024-10-19
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@TableName("tb_order_payment")
public class OrderPaymentPo extends BasePo {

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
     * 订单支付阶段：1-意向金，2-定金，3-尾款
     */
    @TableField("order_payment_phase")
    private Short orderPaymentPhase;

    /**
     * 支付商户
     */
    @TableField("payment_merchant")
    private String paymentMerchant;

    /**
     * 支付内部订单号
     */
    @TableField("payment_order")
    private String paymentOrder;

    /**
     * 支付流水号
     */
    @TableField("payment_reference")
    private String paymentReference;

    /**
     * 支付金额
     */
    @TableField("payment_amount")
    private BigDecimal paymentAmount;

    /**
     * 支付渠道：UNION_PAY-银联，WECHAT-微信，ALIPAY-支付宝
     */
    @TableField("payment_channel")
    private String paymentChannel;

    /**
     * 支付数据类型：1-支付URL，2-URL二维码，3-BASE64图片，4-JSON数据，5-FORM表单数据，6-支付成功URL，7-预下单支付，8-JSAPI小程序数据
     */
    @TableField("payment_data_type")
    private Short paymentDataType;

    /**
     * 支付状态
     */
    @TableField("state")
    private Short state;
}
