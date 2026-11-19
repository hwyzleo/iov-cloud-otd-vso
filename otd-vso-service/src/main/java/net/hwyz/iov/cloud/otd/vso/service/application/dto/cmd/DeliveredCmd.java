package net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DeliveredCmd {
    /**
     * 订单业务 ID（MPT 路径定位使用）
     */
    private String orderId;
    /**
     * 订单号（内部服务兼容定位使用）；orderId/orderNo 必须且只能存在一个
     */
    private String orderNo;
    /**
     * 操作人 ID：MPT 取安全上下文，内部入口为 system
     */
    private String operatorId;
    /**
     * 操作角色：MPT=mpt_operator，内部入口=service
     */
    private String operatorRole;
    /**
     * 操作来源：MPT=mpt，内部入口=delivery_service
     */
    private String operateSource;
    /**
     * 交付备注（可选）
     */
    private String deliveryRemark;
}
