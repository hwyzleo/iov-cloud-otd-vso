package net.hwyz.iov.cloud.otd.vso.api.vo.response;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 订单详情响应对象
 *
 * @author VSO Team
 */
@Data
public class OrderDetailResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 订单业务 ID
     */
    private String orderId;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 小订单号
     */
    private String smallOrderNo;

    /**
     * 订单类型
     */
    private String orderType;

    /**
     * 订单来源
     */
    private String orderSource;

    /**
     * 主状态
     */
    private String mainStatus;

    /**
     * 结束语义
     */
    private String endType;

    /**
     * 客户类型
     */
    private String customerType;

    /**
     * 品牌编码
     */
    private String brandCode;

    /**
     * 区域编码
     */
    private String regionCode;

    /**
     * 门店编码
     */
    private String storeCode;

    /**
     * 销售顾问编码
     */
    private String salesCode;

    /**
     * 绑定 VIN
     */
    private String vehicleVin;

    /**
     * 是否有未关闭异常单
     */
    private Boolean hasException;

    /**
     * 当前版本号
     */
    private Integer currentVersionNo;

    /**
     * 是否锁单中
     */
    private Boolean lockedFlag;

    /**
     * 是否重开过
     */
    private Boolean reopenFlag;

    /**
     * 取消原因
     */
    private String cancelReason;

    /**
     * 关闭原因
     */
    private String closeReason;

    /**
     * 业务创建时间
     */
    private LocalDateTime createdAtBusiness;

    /**
     * 提交审核时间
     */
    private LocalDateTime auditSubmitTime;

    /**
     * 审核通过时间
     */
    private LocalDateTime auditPassTime;

    /**
     * 锁单时间
     */
    private LocalDateTime lockTime;

    /**
     * 交付完成时间
     */
    private LocalDateTime deliveryFinishTime;

    /**
     * 订单完成时间
     */
    private LocalDateTime finishTime;

    /**
     * 取消时间
     */
    private LocalDateTime cancelTime;

    /**
     * 关闭时间
     */
    private LocalDateTime closeTime;

}
