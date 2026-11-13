package net.hwyz.iov.cloud.otd.vso.service.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.PhysicalDeleteResult;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.OrderNotExistException;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Order;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.OrderPurgeMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 订单物理删除服务
 * <p>
 * OTD-VSO-DSN-CR-005：物理删除语义修正为「订单相关数据全部物理清除」。
 * <ul>
 *     <li>不再使用 selectPoByMap + batchPhysicalDeletePo（其 SQL 带 row_valid = 1，
 *     会残留历史逻辑删除行），改为按业务键直接 DELETE；</li>
 *     <li>补齐 vso_refund_item、vso_supplementary_payment、vso_order_agreement_confirmation、
 *     vso_config_change_refund、vso_order_log、vso_order_shadow_delete；</li>
 *     <li>删除前取消该订单的超时任务，避免调度器对已删除订单反复重试；</li>
 *     <li>不再写删除影子审计记录（审计与日志一并清除）。</li>
 * </ul>
 *
 * @author VSO Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderPhysicalDeleteService {

    private static final String SMALL_ORDER_PAY_TIMEOUT_TASK = "SMALL_ORDER_PAY_TIMEOUT";

    private final OrderRepository orderRepository;
    private final OrderPurgeMapper orderPurgeMapper;
    private final TimeoutNotifyService timeoutNotifyService;

    /**
     * 物理删除订单及其全部关联数据（含历史逻辑删除行、审计与日志）
     */
    @Transactional(rollbackFor = Exception.class)
    public PhysicalDeleteResult physicalDeleteOrder(
            String orderId,
            String deleteReason,
            String operatorId,
            Boolean complianceFlag) {

        log.info("开始物理删除订单：orderId={}, operatorId={}, reason={}, complianceFlag={}",
                orderId, operatorId, deleteReason, complianceFlag);

        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new OrderNotExistException(orderId));
        String orderNo = order.getOrderNo();

        LocalDateTime deleteTime = LocalDateTime.now();
        Map<String, Integer> counts = new LinkedHashMap<>();

        // 0. 取消超时任务（Redis 不参与数据库事务，删除失败时任务仍在，由状态守卫兜底）
        try {
            timeoutNotifyService.cancelByOrderIdAndType(orderId, SMALL_ORDER_PAY_TIMEOUT_TASK);
        } catch (Exception e) {
            log.warn("物理删除前取消超时任务失败：orderId={}, reason={}", orderId, e.getMessage());
        }

        // 1. 子表（依赖父表，先删）
        counts.put("vso_order_version_diff", purge("vso_order_version_diff", () -> orderPurgeMapper.deleteOrderVersionDiffByOrderId(orderId)));
        counts.put("vso_order_material_version", purge("vso_order_material_version", () -> orderPurgeMapper.deleteOrderMaterialVersionByOrderId(orderId)));
        counts.put("vso_approval_record", purge("vso_approval_record", () -> orderPurgeMapper.deleteApprovalRecordByOrderId(orderId)));
        counts.put("vso_refund_item", purge("vso_refund_item", () -> orderPurgeMapper.deleteRefundItemByOrderId(orderId)));

        // 2. 按 order_id 的订单数据
        counts.put("vso_order_timeline", purge("vso_order_timeline", () -> orderPurgeMapper.deleteOrderTimelineByOrderId(orderId)));
        counts.put("vso_order_version", purge("vso_order_version", () -> orderPurgeMapper.deleteOrderVersionByOrderId(orderId)));
        counts.put("vso_order_amount", purge("vso_order_amount", () -> orderPurgeMapper.deleteOrderAmountByOrderId(orderId)));
        counts.put("vso_order_party", purge("vso_order_party", () -> orderPurgeMapper.deleteOrderPartyByOrderId(orderId)));
        counts.put("vso_order_vehicle_snapshot", purge("vso_order_vehicle_snapshot", () -> orderPurgeMapper.deleteOrderVehicleSnapshotByOrderId(orderId)));
        counts.put("vso_order_assignment", purge("vso_order_assignment", () -> orderPurgeMapper.deleteOrderAssignmentByOrderId(orderId)));
        counts.put("vso_order_material", purge("vso_order_material", () -> orderPurgeMapper.deleteOrderMaterialByOrderId(orderId)));
        counts.put("vso_order_status_dimension", purge("vso_order_status_dimension", () -> orderPurgeMapper.deleteOrderStatusDimensionByOrderId(orderId)));
        counts.put("vso_order_lock", purge("vso_order_lock", () -> orderPurgeMapper.deleteOrderLockByOrderId(orderId)));
        counts.put("vso_order_agreement_confirmation", purge("vso_order_agreement_confirmation", () -> orderPurgeMapper.deleteOrderAgreementConfirmationByOrderId(orderId)));
        counts.put("vso_supplementary_payment", purge("vso_supplementary_payment", () -> orderPurgeMapper.deleteSupplementaryPaymentByOrderId(orderId)));
        counts.put("vso_config_change_refund", purge("vso_config_change_refund", () -> orderPurgeMapper.deleteConfigChangeRefundByOrderId(orderId)));
        counts.put("vso_vehicle_assignment", purge("vso_vehicle_assignment", () -> orderPurgeMapper.deleteVehicleAssignmentByOrderId(orderId)));
        counts.put("vso_approval", purge("vso_approval", () -> orderPurgeMapper.deleteApprovalByOrderId(orderId)));
        counts.put("vso_exception_order", purge("vso_exception_order", () -> orderPurgeMapper.deleteExceptionOrderByOrderId(orderId)));
        counts.put("vso_contract", purge("vso_contract", () -> orderPurgeMapper.deleteContractByOrderId(orderId)));
        counts.put("vso_payment", purge("vso_payment", () -> orderPurgeMapper.deletePaymentByOrderId(orderId)));
        counts.put("vso_refund", purge("vso_refund", () -> orderPurgeMapper.deleteRefundByOrderId(orderId)));
        counts.put("vso_finance_application", purge("vso_finance_application", () -> orderPurgeMapper.deleteFinanceApplicationByOrderId(orderId)));
        counts.put("vso_subsidy_application", purge("vso_subsidy_application", () -> orderPurgeMapper.deleteSubsidyApplicationByOrderId(orderId)));
        counts.put("vso_delivery_appointment", purge("vso_delivery_appointment", () -> orderPurgeMapper.deleteDeliveryAppointmentByOrderId(orderId)));
        counts.put("vso_delivery_record", purge("vso_delivery_record", () -> orderPurgeMapper.deleteDeliveryRecordByOrderId(orderId)));
        counts.put("vso_registration", purge("vso_registration", () -> orderPurgeMapper.deleteRegistrationByOrderId(orderId)));
        counts.put("vso_invoice", purge("vso_invoice", () -> orderPurgeMapper.deleteInvoiceByOrderId(orderId)));

        // 3. 通知、回调、审计与操作日志
        counts.put("vso_notify_task", purge("vso_notify_task", () -> orderPurgeMapper.deleteNotifyTaskByOrderId(orderId)));
        counts.put("vso_callback_log", purge("vso_callback_log", () -> orderPurgeMapper.deleteCallbackLogByOrderId(orderId)));
        counts.put("vso_audit_log", purge("vso_audit_log", () -> orderPurgeMapper.deleteAuditLogByOrderId(orderId)));
        counts.put("vso_order_log", orderNo == null ? 0 : orderPurgeMapper.deleteOrderLogByOrderNo(orderNo));
        counts.put("vso_order_shadow_delete",
                orderNo == null ? 0 : orderPurgeMapper.deleteShadowDeleteByOrderNo(orderNo));

        // 4. 订单主表（最后）
        counts.put("vso_order", purge("vso_order", () -> orderPurgeMapper.deleteOrderByOrderId(orderId)));

        int totalDeleted = counts.values().stream().mapToInt(Integer::intValue).sum();
        log.info("订单物理删除完成：orderId={}, orderNo={}, totalDeleted={}, detail={}",
                orderId, orderNo, totalDeleted, counts);

        return PhysicalDeleteResult.builder()
                .orderId(orderId)
                .orderNo(orderNo)
                .totalDeletedRecords(totalDeleted)
                .deleteTime(deleteTime)
                .tableDeleteCount(counts)
                .build();
    }

    /**
     * 执行单表清理；表在当前环境不存在（历史淘汰表）时跳过并告警，不阻断整单删除
     */
    private int purge(String table, java.util.function.Supplier<Integer> action) {
        try {
            return action.get();
        } catch (org.springframework.dao.DataAccessException e) {
            if (isMissingTable(e)) {
                log.warn("物理删除跳过不存在的表：table={}", table);
                return 0;
            }
            throw e;
        }
    }

    private boolean isMissingTable(Throwable e) {
        Throwable current = e;
        while (current != null) {
            String message = current.getMessage();
            if (message != null) {
                String lower = message.toLowerCase();
                if (lower.contains("doesn't exist") || lower.contains("unknown table")
                        || lower.contains("table") && lower.contains("not found")) {
                    return true;
                }
            }
            current = current.getCause();
        }
        return false;
    }
}
