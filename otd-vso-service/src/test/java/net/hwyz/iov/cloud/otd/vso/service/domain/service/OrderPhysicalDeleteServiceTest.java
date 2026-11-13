package net.hwyz.iov.cloud.otd.vso.service.domain.service;

import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.PhysicalDeleteResult;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.OrderNotExistException;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Order;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.OrderPurgeMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * 订单物理删除测试（OTD-VSO-DSN-CR-005）
 * <p>
 * 语义：物理删除后不保留任何订单相关数据（含 row_valid=0 历史行、审计与日志）。
 *
 * @author VSO Team
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("物理删除 - 全量清理")
class OrderPhysicalDeleteServiceTest {

    private static final String ORDER_ID = "OID1";
    private static final String ORDER_NO = "SO001";

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderPurgeMapper orderPurgeMapper;
    @Mock
    private TimeoutNotifyService timeoutNotifyService;

    @InjectMocks
    private OrderPhysicalDeleteService orderPhysicalDeleteService;

    @BeforeEach
    void setUp() {
        when(orderRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.of(
                Order.builder().id(ORDER_ID).orderNo(ORDER_NO).build()));
    }

    @Test
    @DisplayName("订单不存在：拒绝删除且不动任何数据")
    void 订单不存在拒绝删除() {
        when(orderRepository.findByOrderId("NOT_EXIST")).thenReturn(Optional.empty());

        assertThrows(OrderNotExistException.class,
                () -> orderPhysicalDeleteService.physicalDeleteOrder("NOT_EXIST", "test", "op", false));
        verifyNoInteractions(orderPurgeMapper);
    }

    @Test
    @DisplayName("全量清理：子表先删、主表最后、取消超时任务、含审计与日志表")
    void 全量清理顺序与范围() {
        PhysicalDeleteResult result = orderPhysicalDeleteService
                .physicalDeleteOrder(ORDER_ID, "test", "op", false);

        // 子表先于父表：退款明细先于退款主单；版本差异先于版本；资料版本先于资料；审批记录先于审批单
        InOrder inOrder = inOrder(orderPurgeMapper);
        inOrder.verify(orderPurgeMapper).deleteOrderVersionDiffByOrderId(ORDER_ID);
        inOrder.verify(orderPurgeMapper).deleteOrderMaterialVersionByOrderId(ORDER_ID);
        inOrder.verify(orderPurgeMapper).deleteApprovalRecordByOrderId(ORDER_ID);
        inOrder.verify(orderPurgeMapper).deleteRefundItemByOrderId(ORDER_ID);
        inOrder.verify(orderPurgeMapper).deleteOrderVersionByOrderId(ORDER_ID);
        inOrder.verify(orderPurgeMapper).deleteOrderMaterialByOrderId(ORDER_ID);
        inOrder.verify(orderPurgeMapper).deleteApprovalByOrderId(ORDER_ID);
        inOrder.verify(orderPurgeMapper).deleteRefundByOrderId(ORDER_ID);
        // 主表最后
        inOrder.verify(orderPurgeMapper).deleteOrderByOrderId(ORDER_ID);

        // 新增覆盖项
        verify(orderPurgeMapper).deleteSupplementaryPaymentByOrderId(ORDER_ID);
        verify(orderPurgeMapper).deleteOrderAgreementConfirmationByOrderId(ORDER_ID);
        verify(orderPurgeMapper).deleteConfigChangeRefundByOrderId(ORDER_ID);
        verify(orderPurgeMapper).deleteOrderLogByOrderNo(ORDER_NO);
        verify(orderPurgeMapper).deleteShadowDeleteByOrderNo(ORDER_NO);
        verify(orderPurgeMapper).deleteAuditLogByOrderId(ORDER_ID);
        verify(orderPurgeMapper).deleteNotifyTaskByOrderId(ORDER_ID);
        verify(orderPurgeMapper).deleteCallbackLogByOrderId(ORDER_ID);

        // 先取消超时任务
        verify(timeoutNotifyService).cancelByOrderIdAndType(ORDER_ID, "SMALL_ORDER_PAY_TIMEOUT");

        // 结果结构
        assertEquals(ORDER_NO, result.getOrderNo());
        assertEquals(34, result.getTableDeleteCount().size(), "应覆盖全部订单相关表");
        assertTrue(result.getTableDeleteCount().containsKey("vso_refund_item"));
        assertTrue(result.getTableDeleteCount().containsKey("vso_order_shadow_delete"));
    }

    @Test
    @DisplayName("删除条数按表汇总")
    void 删除条数汇总() {
        when(orderPurgeMapper.deleteRefundItemByOrderId(ORDER_ID)).thenReturn(2);
        when(orderPurgeMapper.deleteOrderTimelineByOrderId(ORDER_ID)).thenReturn(5);
        when(orderPurgeMapper.deleteOrderLogByOrderNo(ORDER_NO)).thenReturn(1);
        when(orderPurgeMapper.deleteOrderByOrderId(ORDER_ID)).thenReturn(1);

        PhysicalDeleteResult result = orderPhysicalDeleteService
                .physicalDeleteOrder(ORDER_ID, "test", "op", false);

        assertEquals(2, result.getTableDeleteCount().get("vso_refund_item"));
        assertEquals(5, result.getTableDeleteCount().get("vso_order_timeline"));
        assertEquals(1, result.getTableDeleteCount().get("vso_order_log"));
        assertEquals(9, result.getTotalDeletedRecords());
    }

    @Test
    @DisplayName("取消超时任务失败不影响订单数据清理")
    void 取消失败不影响删除() {
        doThrow(new RuntimeException("redis down"))
                .when(timeoutNotifyService).cancelByOrderIdAndType(anyString(), anyString());

        PhysicalDeleteResult result = orderPhysicalDeleteService
                .physicalDeleteOrder(ORDER_ID, "test", "op", false);

        verify(orderPurgeMapper).deleteOrderByOrderId(ORDER_ID);
        assertEquals(ORDER_ID, result.getOrderId());
    }

    @Test
    @DisplayName("订单号为空的脏数据不执行按单号删除")
    void 订单号为空跳过按单号删除() {
        when(orderRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.of(
                Order.builder().id(ORDER_ID).build()));

        orderPhysicalDeleteService.physicalDeleteOrder(ORDER_ID, "test", "op", false);

        verify(orderPurgeMapper, never()).deleteOrderLogByOrderNo(anyString());
        verify(orderPurgeMapper, never()).deleteShadowDeleteByOrderNo(anyString());
        verify(orderPurgeMapper).deleteOrderByOrderId(ORDER_ID);
    }
}
