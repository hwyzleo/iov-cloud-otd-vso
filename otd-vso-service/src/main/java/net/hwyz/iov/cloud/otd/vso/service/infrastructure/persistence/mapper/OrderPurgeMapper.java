package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 订单全量物理清理 Mapper（OTD-VSO-DSN-CR-005）
 * <p>
 * 与各业务 Mapper 的 selectPoByMap + batchPhysicalDeletePo 不同，本 Mapper 直接按业务键硬删除，
 * 不过滤 row_valid，确保订单的所有关联数据（含历史逻辑删除行、审计与日志）一并物理删除。
 * 子表通过父表单号子查询清理，调用顺序由 OrderPhysicalDeleteService 保证。
 *
 * @author VSO Team
 */
@Mapper
public interface OrderPurgeMapper {

    // ==================== 子表（依赖父表，需先删） ====================

    /**
     * 订单版本差异（按订单下的版本 ID）
     */
    int deleteOrderVersionDiffByOrderId(@Param("orderId") String orderId);

    /**
     * 资料版本（按订单下的资料 ID）
     */
    int deleteOrderMaterialVersionByOrderId(@Param("orderId") String orderId);

    /**
     * 审批流转记录（按订单下的审批单 ID）
     */
    int deleteApprovalRecordByOrderId(@Param("orderId") String orderId);

    /**
     * 退款明细（按订单下的退款单 ID）
     */
    int deleteRefundItemByOrderId(@Param("orderId") String orderId);

    // ==================== 按 order_id 的订单数据 ====================

    int deleteOrderTimelineByOrderId(@Param("orderId") String orderId);

    int deleteOrderVersionByOrderId(@Param("orderId") String orderId);

    int deleteOrderAmountByOrderId(@Param("orderId") String orderId);

    int deleteOrderPartyByOrderId(@Param("orderId") String orderId);

    int deleteOrderVehicleSnapshotByOrderId(@Param("orderId") String orderId);

    int deleteOrderAssignmentByOrderId(@Param("orderId") String orderId);

    int deleteOrderMaterialByOrderId(@Param("orderId") String orderId);

    int deleteOrderStatusDimensionByOrderId(@Param("orderId") String orderId);

    int deleteOrderLockByOrderId(@Param("orderId") String orderId);

    int deleteOrderAgreementConfirmationByOrderId(@Param("orderId") String orderId);

    int deleteSupplementaryPaymentByOrderId(@Param("orderId") String orderId);

    int deleteConfigChangeRefundByOrderId(@Param("orderId") String orderId);

    int deleteVehicleAssignmentByOrderId(@Param("orderId") String orderId);

    int deleteApprovalByOrderId(@Param("orderId") String orderId);

    int deleteExceptionOrderByOrderId(@Param("orderId") String orderId);

    int deleteContractByOrderId(@Param("orderId") String orderId);

    int deletePaymentByOrderId(@Param("orderId") String orderId);

    int deleteRefundByOrderId(@Param("orderId") String orderId);

    int deleteFinanceApplicationByOrderId(@Param("orderId") String orderId);

    int deleteSubsidyApplicationByOrderId(@Param("orderId") String orderId);

    int deleteDeliveryAppointmentByOrderId(@Param("orderId") String orderId);

    int deleteDeliveryRecordByOrderId(@Param("orderId") String orderId);

    int deleteRegistrationByOrderId(@Param("orderId") String orderId);

    int deleteInvoiceByOrderId(@Param("orderId") String orderId);

    // ==================== 通知、日志与审计 ====================

    int deleteNotifyTaskByOrderId(@Param("orderId") String orderId);

    int deleteCallbackLogByOrderId(@Param("orderId") String orderId);

    int deleteAuditLogByOrderId(@Param("orderId") String orderId);

    /**
     * 订单操作日志（vso_order_log 以订单号为业务键）
     */
    int deleteOrderLogByOrderNo(@Param("orderNo") String orderNo);

    /**
     * 删除影子审计记录（物理删除后不保留任何订单相关数据）
     */
    int deleteShadowDeleteByOrderNo(@Param("orderNo") String orderNo);

    // ==================== 订单主表（最后执行） ====================

    int deleteOrderByOrderId(@Param("orderId") String orderId);

}
