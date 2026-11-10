package net.hwyz.iov.cloud.otd.vso.service.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.hwyz.iov.cloud.framework.common.exception.ErrorCode;

/**
 * VSO 车辆销售订单服务错误码
 *
 * @author hwyz_leo
 */
@Getter
@AllArgsConstructor
public enum VsoErrorCode implements ErrorCode {

    // 通用
    INVALID_PARAM("201001", "参数校验失败"),
    INTERNAL_ERROR("201002", "系统内部错误"),

    // 销售型号配置
    SALE_MODEL_CONFIG_TYPE_CODE_NOT_EXIST("201003", "销售型号配置类型编码不存在"),
    CONFIGURATION_CODE_NOT_EXIST("201004", "配置编码不存在"),
    SALE_MODEL_NOT_EXIST("201005", "销售型号不存在"),

    // 订单
    ORDER_NOT_EXIST("201006", "订单不存在"),
    ORDER_ILLEGAL_DELETE("201007", "订单非法删除"),
    ORDER_STATE_NOT_ALLOWED("201008", "订单状态不允许"),
    ACCOUNT_NOT_EXIST("201009", "账号不存在"),
    CONFIGURATION_HAS_LOCKED("201010", "订单配置已锁定"),

    // 心愿单
    WISHLIST_NOT_EXIST("201011", "心愿单不存在"),
    CONFIGURATION_NOT_MATCHED("201012", "OptionCode 组合无法匹配到合法 Configuration"),

    // 支付
    PAYMENT_CHANNEL_NOT_AVAILABLE("201013", "支付渠道不可用"),
    PAYMENT_NOT_EXIST("201014", "支付不存在"),
    PAYMENT_STATUS_MISMATCH("201015", "支付状态不匹配"),
    SUPPLEMENT_PAYMENT_NOT_EXIST("201016", "补缴支付不存在"),
    SUPPLEMENT_PAYMENT_STATUS_NOT_ALLOWED("201017", "补缴支付状态不允许"),
    SUPPLEMENT_PAYMENT_EXPIRED("201018", "补缴支付已过期"),
    CONFIG_CHANGE_REFUND_NOT_EXIST("201019", "配置变更退款不存在"),
    CONFIG_CHANGE_REFUND_FAILED("201020", "配置变更退款失败"),

    // 订单限制
    DUPLICATE_UNPAID_ORDER("201021", "重复未支付订单"),
    WISHLIST_LIMIT_EXCEEDED("201022", "心愿单数量超限"),
    DUPLICATE_WISHLIST("201023", "重复心愿单"),

    // 车辆
    VIN_CONFLICT("201024", "VIN冲突"),
    VIN_INVALID("201025", "VIN无效"),

    // 审批
    AUDIT_RESUBMIT_LIMIT_EXCEEDED("201026", "审批重提交次数超限"),
    AUDIT_REJECT_REASON_REQUIRED("201027", "审批拒绝原因必填"),

    // 冲突
    PAYMENT_CONFLICT("201028", "支付冲突"),
    LOCK_CONFLICT("201029", "锁定冲突"),
    BIND_CONFLICT("201030", "绑定冲突"),

    // Configuration 销售
    CONFIGURATION_NOT_FOR_SALE("201034", "Configuration 可生产但未列入销售白名单"),

    // OptionCode 销售
    OPTION_NOT_FOR_SALE("201035", "OptionCode 在销售策略中处于 off_shelf 状态或未配置价格"),
    OPTION_REGION_RESTRICTED("201036", "OptionCode 当前用户区域不可售"),

    // 销售车型
    SALE_MODEL_VARIANT_LOCKED("201037", "SaleModel 已有活跃订单或心愿单，不可修改 variantCode"),

    // MDM 投影
    MDM_PROJECTION_STALE("201038", "MDM 本地投影过期或不一致，需触发强制同步"),

    // Model/Variant 销售
    MODEL_NOT_FOR_SALE("301040", "Model 销售策略校验失败"),
    VARIANT_NOT_FOR_SALE("301041", "Variant 销售策略校验失败"),

    // OTD-VSO-DSN-CR-002 C 端购车全流程
    AGREEMENT_NOT_EXIST("201039", "购车协议不存在、失效或版本不匹配"),
    AGREEMENT_CONFIRMATION_REQUIRED("201040", "必确认购车协议未提交"),
    ORDER_ACCESS_DENIED("201041", "订单不属于当前账号"),
    REFUND_NOT_EXIST("201042", "退款单不存在"),
    REFUND_STATUS_NOT_ALLOWED("201043", "当前退款状态不允许操作"),
    REFUND_CONFLICT("201044", "重复或冲突退款"),
    DELIVERY_APPOINTMENT_NOT_EXIST("201045", "交付预约不存在"),
    DELIVERY_APPOINTMENT_CONFLICT("201046", "已有有效交付预约"),
    DELIVERY_APPOINTMENT_STATUS_NOT_ALLOWED("201047", "交付预约状态不允许操作"),
    CALLBACK_SIGNATURE_INVALID("201048", "支付或退款回调签名/防重放失败"),
    PAYMENT_AMOUNT_MISMATCH("201049", "支付、补款或退款金额不一致"),
    ORDER_AMOUNT_SNAPSHOT_MISSING("201050", "历史订单金额快照缺失，需人工修复"),
    PAYMENT_EXPIRED("201051", "支付单已过期"),
    REFUND_AMOUNT_INVALID("201052", "退款金额或分摊不合法"),
    EXTERNAL_GATEWAY_FAILED("201053", "外部支付或退款网关调用失败"),

    // 后台协议管理
    AGREEMENT_VERSION_CONFLICT("201054", "该车型下协议版本已存在");

    private final String code;
    private final String message;
}
