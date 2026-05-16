package net.hwyz.iov.cloud.otd.vso.service.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.PhysicalDeleteResult;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.OrderNotExistException;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Order;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.*;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单物理删除服务
 *
 * @author VSO Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderPhysicalDeleteService {

    private final OrderRepository orderRepository;

    // 第一层：通知、任务、日志、版本相关 Mapper
    private final NotifyTaskMapper notifyTaskMapper;
    private final CallbackLogMapper callbackLogMapper;
    private final AuditLogMapper auditLogMapper;
    private final OrderTimelineMapper orderTimelineMapper;
    private final OrderVersionMapper orderVersionMapper;
    private final OrderVersionDiffMapper orderVersionDiffMapper;

    // 第二层：审批、异常、流程单据 Mapper
    private final ApprovalMapper approvalMapper;
    private final ApprovalRecordMapper approvalRecordMapper;
    private final ExceptionOrderMapper exceptionOrderMapper;
    private final OrderLockMapper orderLockMapper;
    private final VehicleAssignmentMapper vehicleAssignmentMapper;
    private final ContractMapper contractMapper;
    private final PaymentMapper paymentMapper;
    private final RefundMapper refundMapper;
    private final FinanceApplicationMapper financeApplicationMapper;
    private final SubsidyApplicationMapper subsidyApplicationMapper;
    private final DeliveryAppointmentMapper deliveryAppointmentMapper;
    private final DeliveryRecordMapper deliveryRecordMapper;
    private final RegistrationMapper registrationMapper;
    private final InvoiceMapper invoiceMapper;

    // 第三层：资料 Mapper
    private final OrderMaterialMapper orderMaterialMapper;
    private final OrderMaterialVersionMapper orderMaterialVersionMapper;

    // 第四层：核心业务 Mapper
    private final OrderAmountMapper orderAmountMapper;
    private final OrderStatusDimensionMapper orderStatusDimensionMapper;
    private final OrderAssignmentMapper orderAssignmentMapper;
    private final OrderVehicleSnapshotMapper orderVehicleSnapshotMapper;
    private final OrderPartyMapper orderPartyMapper;
    private final OrderTransformMapper orderTransformMapper;

    // 第五层：订单主表 Mapper
    private final OrderMapper orderMapper;

    // 审计影子 Mapper
    private final OrderShadowDeleteMapper orderShadowDeleteMapper;

    /**
     * 物理删除订单及其所有关联数据
     */
    @Transactional(rollbackFor = Exception.class)
    public PhysicalDeleteResult physicalDeleteOrder(
            String orderId,
            String deleteReason,
            String operatorId,
            Boolean complianceFlag) {

        log.info("开始物理删除订单：orderId={}, operatorId={}, reason={}", orderId, operatorId, deleteReason);

        // 加载订单信息
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new OrderNotExistException(orderId));

        String orderNo = order.getOrderNo();
        Integer beforeOrderState = order.getOrderState() != null ? order.getOrderState().getValue() : null;

        LocalDateTime deleteTime = LocalDateTime.now();
        Map<String, Integer> tableDeleteCount = new HashMap<>();
        int totalDeleted = 0;

        // 第一层：删除通知、任务、日志、版本相关表
        totalDeleted += deleteNotifyTasks(orderId, tableDeleteCount);
        totalDeleted += deleteCallbackLogs(orderId, tableDeleteCount);
        totalDeleted += deleteAuditLogs(orderId, tableDeleteCount);
        totalDeleted += deleteOrderTimelines(orderId, tableDeleteCount);
        totalDeleted += deleteOrderVersions(orderId, tableDeleteCount);
        totalDeleted += deleteOrderVersionDiffs(orderId, tableDeleteCount);

        // 第二层：删除审批、异常、流程单据表
        totalDeleted += deleteApprovals(orderId, tableDeleteCount);
        totalDeleted += deleteExceptionOrders(orderId, tableDeleteCount);
        totalDeleted += deleteOrderLocks(orderId, tableDeleteCount);
        totalDeleted += deleteVehicleAssignments(orderId, tableDeleteCount);
        totalDeleted += deleteContracts(orderId, tableDeleteCount);
        totalDeleted += deletePayments(orderId, tableDeleteCount);
        totalDeleted += deleteRefunds(orderId, tableDeleteCount);
        totalDeleted += deleteFinanceApplications(orderId, tableDeleteCount);
        totalDeleted += deleteSubsidyApplications(orderId, tableDeleteCount);
        totalDeleted += deleteDeliveryAppointments(orderId, tableDeleteCount);
        totalDeleted += deleteDeliveryRecords(orderId, tableDeleteCount);
        totalDeleted += deleteRegistrations(orderId, tableDeleteCount);
        totalDeleted += deleteInvoices(orderId, tableDeleteCount);

        // 第三层：删除资料表
        totalDeleted += deleteOrderMaterials(orderId, tableDeleteCount);

        // 第四层：删除核心业务表
        totalDeleted += deleteOrderAmounts(orderId, tableDeleteCount);
        totalDeleted += deleteOrderStatusDimensions(orderId, tableDeleteCount);
        totalDeleted += deleteOrderAssignments(orderId, tableDeleteCount);
        totalDeleted += deleteOrderVehicleSnapshots(orderId, tableDeleteCount);
        totalDeleted += deleteOrderParties(orderId, tableDeleteCount);
        totalDeleted += deleteOrderTransforms(orderId, tableDeleteCount);

        // 第五层：删除订单主表
        totalDeleted += deleteOrder(orderId, tableDeleteCount);

        // 最后：保存审计影子记录
        saveShadowDeleteRecord(orderId, orderNo, beforeOrderState,
                deleteReason, operatorId, complianceFlag, deleteTime);

        log.info("订单物理删除完成：orderId={}, totalDeleted={}", orderId, totalDeleted);

        return PhysicalDeleteResult.builder()
                .orderId(orderId)
                .orderNo(orderNo)
                .totalDeletedRecords(totalDeleted)
                .deleteTime(deleteTime)
                .tableDeleteCount(tableDeleteCount)
                .build();
    }

    private int deleteNotifyTasks(String orderId, Map<String, Integer> tableDeleteCount) {
        Map<String, Object> params = new HashMap<>();
        params.put("orderId", orderId);
        List<NotifyTaskPo> list = notifyTaskMapper.selectPoByMap(params);
        if (list == null || list.isEmpty()) {
            tableDeleteCount.put("vso_notify_task", 0);
            return 0;
        }
        Long[] ids = list.stream().map(NotifyTaskPo::getId).toArray(Long[]::new);
        int count = notifyTaskMapper.batchPhysicalDeletePo(ids);
        tableDeleteCount.put("vso_notify_task", count);
        return count;
    }

    private int deleteCallbackLogs(String orderId, Map<String, Integer> tableDeleteCount) {
        Map<String, Object> params = new HashMap<>();
        params.put("orderId", orderId);
        List<CallbackLogPo> list = callbackLogMapper.selectPoByMap(params);
        if (list == null || list.isEmpty()) {
            tableDeleteCount.put("vso_callback_log", 0);
            return 0;
        }
        Long[] ids = list.stream().map(CallbackLogPo::getId).toArray(Long[]::new);
        int count = callbackLogMapper.batchPhysicalDeletePo(ids);
        tableDeleteCount.put("vso_callback_log", count);
        return count;
    }

    private int deleteAuditLogs(String orderId, Map<String, Integer> tableDeleteCount) {
        Map<String, Object> params = new HashMap<>();
        params.put("orderId", orderId);
        List<AuditLogPo> list = auditLogMapper.selectPoByMap(params);
        if (list == null || list.isEmpty()) {
            tableDeleteCount.put("vso_audit_log", 0);
            return 0;
        }
        Long[] ids = list.stream().map(AuditLogPo::getId).toArray(Long[]::new);
        int count = auditLogMapper.batchPhysicalDeletePo(ids);
        tableDeleteCount.put("vso_audit_log", count);
        return count;
    }

    private int deleteOrderTimelines(String orderId, Map<String, Integer> tableDeleteCount) {
        Map<String, Object> params = new HashMap<>();
        params.put("orderId", orderId);
        List<OrderTimelinePo> list = orderTimelineMapper.selectPoByMap(params);
        if (list == null || list.isEmpty()) {
            tableDeleteCount.put("vso_order_timeline", 0);
            return 0;
        }
        Long[] ids = list.stream().map(OrderTimelinePo::getId).toArray(Long[]::new);
        int count = orderTimelineMapper.batchPhysicalDeletePo(ids);
        tableDeleteCount.put("vso_order_timeline", count);
        return count;
    }

    private int deleteOrderVersions(String orderId, Map<String, Integer> tableDeleteCount) {
        Map<String, Object> params = new HashMap<>();
        params.put("orderId", orderId);
        List<OrderVersionPo> list = orderVersionMapper.selectPoByMap(params);
        if (list == null || list.isEmpty()) {
            tableDeleteCount.put("vso_order_version", 0);
            return 0;
        }
        Long[] ids = list.stream().map(OrderVersionPo::getId).toArray(Long[]::new);
        int count = orderVersionMapper.batchPhysicalDeletePo(ids);
        tableDeleteCount.put("vso_order_version", count);
        return count;
    }

    private int deleteOrderVersionDiffs(String orderId, Map<String, Integer> tableDeleteCount) {
        // 先查询订单关联的版本
        Map<String, Object> versionParams = new HashMap<>();
        versionParams.put("orderId", orderId);
        List<OrderVersionPo> versions = orderVersionMapper.selectPoByMap(versionParams);
        if (versions == null || versions.isEmpty()) {
            tableDeleteCount.put("vso_order_version_diff", 0);
            return 0;
        }
        int count = 0;
        for (OrderVersionPo version : versions) {
            Map<String, Object> params = new HashMap<>();
            params.put("versionId", version.getOrderVersionId());
            List<OrderVersionDiffPo> list = orderVersionDiffMapper.selectPoByMap(params);
            if (list != null && !list.isEmpty()) {
                Long[] ids = list.stream().map(OrderVersionDiffPo::getId).toArray(Long[]::new);
                count += orderVersionDiffMapper.batchPhysicalDeletePo(ids);
            }
        }
        tableDeleteCount.put("vso_order_version_diff", count);
        return count;
    }

    private int deleteApprovals(String orderId, Map<String, Integer> tableDeleteCount) {
        Map<String, Object> params = new HashMap<>();
        params.put("orderId", orderId);
        List<ApprovalPo> list = approvalMapper.selectPoByMap(params);
        if (list == null || list.isEmpty()) {
            tableDeleteCount.put("vso_approval", 0);
            tableDeleteCount.put("vso_approval_record", 0);
            return 0;
        }
        // 先删除审批流转记录
        int recordCount = 0;
        for (ApprovalPo approval : list) {
            List<ApprovalRecordPo> records = approvalRecordMapper.selectByApprovalId(approval.getApprovalId());
            if (records != null && !records.isEmpty()) {
                Long[] recordIds = records.stream().map(ApprovalRecordPo::getId).toArray(Long[]::new);
                recordCount += approvalRecordMapper.batchPhysicalDeletePo(recordIds);
            }
        }
        tableDeleteCount.put("vso_approval_record", recordCount);
        // 再删除审批单
        Long[] ids = list.stream().map(ApprovalPo::getId).toArray(Long[]::new);
        int count = approvalMapper.batchPhysicalDeletePo(ids);
        tableDeleteCount.put("vso_approval", count);
        return count + recordCount;
    }

    private int deleteExceptionOrders(String orderId, Map<String, Integer> tableDeleteCount) {
        Map<String, Object> params = new HashMap<>();
        params.put("orderId", orderId);
        List<ExceptionOrderPo> list = exceptionOrderMapper.selectPoByMap(params);
        if (list == null || list.isEmpty()) {
            tableDeleteCount.put("vso_exception_order", 0);
            return 0;
        }
        Long[] ids = list.stream().map(ExceptionOrderPo::getId).toArray(Long[]::new);
        int count = exceptionOrderMapper.batchPhysicalDeletePo(ids);
        tableDeleteCount.put("vso_exception_order", count);
        return count;
    }

    private int deleteOrderLocks(String orderId, Map<String, Integer> tableDeleteCount) {
        Map<String, Object> params = new HashMap<>();
        params.put("orderId", orderId);
        List<OrderLockPo> list = orderLockMapper.selectPoByMap(params);
        if (list == null || list.isEmpty()) {
            tableDeleteCount.put("vso_order_lock", 0);
            return 0;
        }
        Long[] ids = list.stream().map(OrderLockPo::getId).toArray(Long[]::new);
        int count = orderLockMapper.batchPhysicalDeletePo(ids);
        tableDeleteCount.put("vso_order_lock", count);
        return count;
    }

    private int deleteVehicleAssignments(String orderId, Map<String, Integer> tableDeleteCount) {
        Map<String, Object> params = new HashMap<>();
        params.put("orderId", orderId);
        List<VehicleAssignmentPo> list = vehicleAssignmentMapper.selectPoByMap(params);
        if (list == null || list.isEmpty()) {
            tableDeleteCount.put("vso_vehicle_assignment", 0);
            return 0;
        }
        Long[] ids = list.stream().map(VehicleAssignmentPo::getId).toArray(Long[]::new);
        int count = vehicleAssignmentMapper.batchPhysicalDeletePo(ids);
        tableDeleteCount.put("vso_vehicle_assignment", count);
        return count;
    }

    private int deleteContracts(String orderId, Map<String, Integer> tableDeleteCount) {
        Map<String, Object> params = new HashMap<>();
        params.put("orderId", orderId);
        List<ContractPo> list = contractMapper.selectPoByMap(params);
        if (list == null || list.isEmpty()) {
            tableDeleteCount.put("vso_contract", 0);
            return 0;
        }
        Long[] ids = list.stream().map(ContractPo::getId).toArray(Long[]::new);
        int count = contractMapper.batchPhysicalDeletePo(ids);
        tableDeleteCount.put("vso_contract", count);
        return count;
    }

    private int deletePayments(String orderId, Map<String, Integer> tableDeleteCount) {
        Map<String, Object> params = new HashMap<>();
        params.put("orderId", orderId);
        List<PaymentPo> list = paymentMapper.selectPoByMap(params);
        if (list == null || list.isEmpty()) {
            tableDeleteCount.put("vso_payment", 0);
            return 0;
        }
        Long[] ids = list.stream().map(PaymentPo::getId).toArray(Long[]::new);
        int count = paymentMapper.batchPhysicalDeletePo(ids);
        tableDeleteCount.put("vso_payment", count);
        return count;
    }

    private int deleteRefunds(String orderId, Map<String, Integer> tableDeleteCount) {
        Map<String, Object> params = new HashMap<>();
        params.put("orderId", orderId);
        List<RefundPo> list = refundMapper.selectPoByMap(params);
        if (list == null || list.isEmpty()) {
            tableDeleteCount.put("vso_refund", 0);
            return 0;
        }
        Long[] ids = list.stream().map(RefundPo::getId).toArray(Long[]::new);
        int count = refundMapper.batchPhysicalDeletePo(ids);
        tableDeleteCount.put("vso_refund", count);
        return count;
    }

    private int deleteFinanceApplications(String orderId, Map<String, Integer> tableDeleteCount) {
        Map<String, Object> params = new HashMap<>();
        params.put("orderId", orderId);
        List<FinanceApplicationPo> list = financeApplicationMapper.selectPoByMap(params);
        if (list == null || list.isEmpty()) {
            tableDeleteCount.put("vso_finance_application", 0);
            return 0;
        }
        Long[] ids = list.stream().map(FinanceApplicationPo::getId).toArray(Long[]::new);
        int count = financeApplicationMapper.batchPhysicalDeletePo(ids);
        tableDeleteCount.put("vso_finance_application", count);
        return count;
    }

    private int deleteSubsidyApplications(String orderId, Map<String, Integer> tableDeleteCount) {
        Map<String, Object> params = new HashMap<>();
        params.put("orderId", orderId);
        List<SubsidyApplicationPo> list = subsidyApplicationMapper.selectPoByMap(params);
        if (list == null || list.isEmpty()) {
            tableDeleteCount.put("vso_subsidy_application", 0);
            return 0;
        }
        Long[] ids = list.stream().map(SubsidyApplicationPo::getId).toArray(Long[]::new);
        int count = subsidyApplicationMapper.batchPhysicalDeletePo(ids);
        tableDeleteCount.put("vso_subsidy_application", count);
        return count;
    }

    private int deleteDeliveryAppointments(String orderId, Map<String, Integer> tableDeleteCount) {
        Map<String, Object> params = new HashMap<>();
        params.put("orderId", orderId);
        List<DeliveryAppointmentPo> list = deliveryAppointmentMapper.selectPoByMap(params);
        if (list == null || list.isEmpty()) {
            tableDeleteCount.put("vso_delivery_appointment", 0);
            return 0;
        }
        Long[] ids = list.stream().map(DeliveryAppointmentPo::getId).toArray(Long[]::new);
        int count = deliveryAppointmentMapper.batchPhysicalDeletePo(ids);
        tableDeleteCount.put("vso_delivery_appointment", count);
        return count;
    }

    private int deleteDeliveryRecords(String orderId, Map<String, Integer> tableDeleteCount) {
        Map<String, Object> params = new HashMap<>();
        params.put("orderId", orderId);
        List<DeliveryRecordPo> list = deliveryRecordMapper.selectPoByMap(params);
        if (list == null || list.isEmpty()) {
            tableDeleteCount.put("vso_delivery_record", 0);
            return 0;
        }
        Long[] ids = list.stream().map(DeliveryRecordPo::getId).toArray(Long[]::new);
        int count = deliveryRecordMapper.batchPhysicalDeletePo(ids);
        tableDeleteCount.put("vso_delivery_record", count);
        return count;
    }

    private int deleteRegistrations(String orderId, Map<String, Integer> tableDeleteCount) {
        Map<String, Object> params = new HashMap<>();
        params.put("orderId", orderId);
        List<RegistrationPo> list = registrationMapper.selectPoByMap(params);
        if (list == null || list.isEmpty()) {
            tableDeleteCount.put("vso_registration", 0);
            return 0;
        }
        Long[] ids = list.stream().map(RegistrationPo::getId).toArray(Long[]::new);
        int count = registrationMapper.batchPhysicalDeletePo(ids);
        tableDeleteCount.put("vso_registration", count);
        return count;
    }

    private int deleteInvoices(String orderId, Map<String, Integer> tableDeleteCount) {
        Map<String, Object> params = new HashMap<>();
        params.put("orderId", orderId);
        List<InvoicePo> list = invoiceMapper.selectPoByMap(params);
        if (list == null || list.isEmpty()) {
            tableDeleteCount.put("vso_invoice", 0);
            return 0;
        }
        Long[] ids = list.stream().map(InvoicePo::getId).toArray(Long[]::new);
        int count = invoiceMapper.batchPhysicalDeletePo(ids);
        tableDeleteCount.put("vso_invoice", count);
        return count;
    }

    private int deleteOrderMaterials(String orderId, Map<String, Integer> tableDeleteCount) {
        Map<String, Object> params = new HashMap<>();
        params.put("orderId", orderId);
        List<OrderMaterialPo> list = orderMaterialMapper.selectPoByMap(params);
        if (list == null || list.isEmpty()) {
            tableDeleteCount.put("vso_order_material", 0);
            tableDeleteCount.put("vso_order_material_version", 0);
            return 0;
        }
        // 先删除资料版本
        int versionCount = 0;
        for (OrderMaterialPo material : list) {
            Map<String, Object> versionParams = new HashMap<>();
            versionParams.put("materialId", material.getMaterialId());
            List<OrderMaterialVersionPo> versions = orderMaterialVersionMapper.selectPoByMap(versionParams);
            if (versions != null && !versions.isEmpty()) {
                Long[] versionIds = versions.stream().map(OrderMaterialVersionPo::getId).toArray(Long[]::new);
                versionCount += orderMaterialVersionMapper.batchPhysicalDeletePo(versionIds);
            }
        }
        tableDeleteCount.put("vso_order_material_version", versionCount);
        // 再删除资料
        Long[] ids = list.stream().map(OrderMaterialPo::getId).toArray(Long[]::new);
        int count = orderMaterialMapper.batchPhysicalDeletePo(ids);
        tableDeleteCount.put("vso_order_material", count);
        return count + versionCount;
    }

    private int deleteOrderAmounts(String orderId, Map<String, Integer> tableDeleteCount) {
        Map<String, Object> params = new HashMap<>();
        params.put("orderId", orderId);
        List<OrderAmountPo> list = orderAmountMapper.selectPoByMap(params);
        if (list == null || list.isEmpty()) {
            tableDeleteCount.put("vso_order_amount", 0);
            return 0;
        }
        Long[] ids = list.stream().map(OrderAmountPo::getId).toArray(Long[]::new);
        int count = orderAmountMapper.batchPhysicalDeletePo(ids);
        tableDeleteCount.put("vso_order_amount", count);
        return count;
    }

    private int deleteOrderStatusDimensions(String orderId, Map<String, Integer> tableDeleteCount) {
        Map<String, Object> params = new HashMap<>();
        params.put("orderId", orderId);
        List<OrderStatusDimensionPo> list = orderStatusDimensionMapper.selectPoByMap(params);
        if (list == null || list.isEmpty()) {
            tableDeleteCount.put("vso_order_status_dimension", 0);
            return 0;
        }
        Long[] ids = list.stream().map(OrderStatusDimensionPo::getId).toArray(Long[]::new);
        int count = orderStatusDimensionMapper.batchPhysicalDeletePo(ids);
        tableDeleteCount.put("vso_order_status_dimension", count);
        return count;
    }

    private int deleteOrderAssignments(String orderId, Map<String, Integer> tableDeleteCount) {
        Map<String, Object> params = new HashMap<>();
        params.put("orderId", orderId);
        List<OrderAssignmentPo> list = orderAssignmentMapper.selectPoByMap(params);
        if (list == null || list.isEmpty()) {
            tableDeleteCount.put("vso_order_assignment", 0);
            return 0;
        }
        Long[] ids = list.stream().map(OrderAssignmentPo::getId).toArray(Long[]::new);
        int count = orderAssignmentMapper.batchPhysicalDeletePo(ids);
        tableDeleteCount.put("vso_order_assignment", count);
        return count;
    }

    private int deleteOrderVehicleSnapshots(String orderId, Map<String, Integer> tableDeleteCount) {
        Map<String, Object> params = new HashMap<>();
        params.put("orderId", orderId);
        List<OrderVehicleSnapshotPo> list = orderVehicleSnapshotMapper.selectPoByMap(params);
        if (list == null || list.isEmpty()) {
            tableDeleteCount.put("vso_order_vehicle_snapshot", 0);
            return 0;
        }
        Long[] ids = list.stream().map(OrderVehicleSnapshotPo::getId).toArray(Long[]::new);
        int count = orderVehicleSnapshotMapper.batchPhysicalDeletePo(ids);
        tableDeleteCount.put("vso_order_vehicle_snapshot", count);
        return count;
    }

    private int deleteOrderParties(String orderId, Map<String, Integer> tableDeleteCount) {
        Map<String, Object> params = new HashMap<>();
        params.put("orderId", orderId);
        List<OrderPartyPo> list = orderPartyMapper.selectPoByMap(params);
        if (list == null || list.isEmpty()) {
            tableDeleteCount.put("vso_order_party", 0);
            return 0;
        }
        Long[] ids = list.stream().map(OrderPartyPo::getId).toArray(Long[]::new);
        int count = orderPartyMapper.batchPhysicalDeletePo(ids);
        tableDeleteCount.put("vso_order_party", count);
        return count;
    }

    private int deleteOrderTransforms(String orderId, Map<String, Integer> tableDeleteCount) {
        // vso_order_transform 表已在 V1.16.0 迁移中删除，直接返回 0
        tableDeleteCount.put("vso_order_transform", 0);
        return 0;
    }

    private int deleteOrder(String orderId, Map<String, Integer> tableDeleteCount) {
        Map<String, Object> params = new HashMap<>();
        params.put("orderId", orderId);
        List<OrderPo> list = orderMapper.selectPoByMap(params);
        if (list == null || list.isEmpty()) {
            tableDeleteCount.put("vso_order", 0);
            return 0;
        }
        Long[] ids = list.stream().map(OrderPo::getId).toArray(Long[]::new);
        int count = orderMapper.batchPhysicalDeletePo(ids);
        tableDeleteCount.put("vso_order", count);
        return count;
    }

    /**
     * 保存审计影子记录
     */
    private void saveShadowDeleteRecord(
            String orderId,
            String orderNo,
            Integer beforeOrderState,
            String deleteReason,
            String operatorId,
            Boolean complianceFlag,
            LocalDateTime deleteTime) {

        OrderShadowDeletePo shadowPo = new OrderShadowDeletePo();
        shadowPo.setShadowDeleteId("SD" + System.currentTimeMillis());
        shadowPo.setOriginOrderNo(orderNo);
        shadowPo.setDeleteApprovalId(null);
        shadowPo.setDeleteReason(deleteReason);
        shadowPo.setBeforeOrderState(beforeOrderState);
        shadowPo.setComplianceDeleteFlag(complianceFlag != null && complianceFlag ? 1 : 0);
        shadowPo.setDeleteUserId(operatorId);
        shadowPo.setDeleteTime(deleteTime);
        shadowPo.setRowValid(1);

        orderShadowDeleteMapper.insertPo(shadowPo);

        log.info("保存审计影子记录：orderId={}, shadowId={}", orderId, shadowPo.getShadowDeleteId());
    }
}
