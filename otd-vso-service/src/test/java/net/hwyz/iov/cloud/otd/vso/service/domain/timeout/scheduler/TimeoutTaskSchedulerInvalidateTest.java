package net.hwyz.iov.cloud.otd.vso.service.domain.timeout.scheduler;

import net.hwyz.iov.cloud.otd.vso.service.domain.model.TimeoutTask;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.AuditRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.SupplementaryPaymentRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.service.OrderDomainService;
import net.hwyz.iov.cloud.otd.vso.service.domain.service.TimeoutNotifyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 超时任务调度器测试（OTD-VSO-DSN-CR-005）
 * <p>
 * 缺陷回归：同一到期任务被反复执行，导致每分钟写一条 ORDER_INVALIDATE 时间线。
 * 修复后任务执行一次即从待触发集合移除（由 TimeoutNotifyService 保证）。
 *
 * @author VSO Team
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("超时调度器 - 小订单失效一次性触发")
class TimeoutTaskSchedulerInvalidateTest {

    @Mock
    private TimeoutNotifyService timeoutNotifyService;
    @Mock
    private OrderDomainService orderDomainService;
    @Mock
    private SupplementaryPaymentRepository supplementaryPaymentRepository;

    @InjectMocks
    private TimeoutTaskScheduler timeoutTaskScheduler;

    private TimeoutTask invalidateTask;

    @BeforeEach
    void setUp() {
        invalidateTask = new TimeoutTask("TK1", "OID1", "SMALL_ORDER_PAY_TIMEOUT", "invalid", 30);
        invalidateTask.setPlanTriggerTime(LocalDateTime.now().minusMinutes(1));
        invalidateTask.trigger();
    }

    @Test
    @DisplayName("到期任务只失效一次：第二次轮询不再触发")
    void 只失效一次() {
        // 第一次轮询返回到期任务；修复后任务被消费，第二次轮询为空
        when(timeoutNotifyService.getExpiredTasks())
                .thenReturn(List.of(invalidateTask))
                .thenReturn(Collections.emptyList());
        when(orderDomainService.invalidateSmallOrder("OID1")).thenReturn(true);

        timeoutTaskScheduler.checkExpiredTasks();
        timeoutTaskScheduler.checkExpiredTasks();

        verify(orderDomainService, times(1)).invalidateSmallOrder("OID1");
        verify(timeoutNotifyService, times(1)).completeTimeoutTask("TK1");
    }

    @Test
    @DisplayName("非待支付订单跳过失效，但仍消费任务")
    void 跳过失效仍消费任务() {
        when(timeoutNotifyService.getExpiredTasks()).thenReturn(List.of(invalidateTask));
        when(orderDomainService.invalidateSmallOrder("OID1")).thenReturn(false);

        timeoutTaskScheduler.checkExpiredTasks();

        verify(orderDomainService).invalidateSmallOrder("OID1");
        verify(timeoutNotifyService).completeTimeoutTask("TK1");
    }

    @Test
    @DisplayName("处理异常走失败重试，不再无限重复")
    void 异常走失败重试() {
        when(timeoutNotifyService.getExpiredTasks()).thenReturn(List.of(invalidateTask));
        when(orderDomainService.invalidateSmallOrder(anyString()))
                .thenThrow(new IllegalStateException("db down"));

        timeoutTaskScheduler.checkExpiredTasks();

        verify(timeoutNotifyService).failTimeoutTask(invalidateTask);
        verify(timeoutNotifyService, times(0)).completeTimeoutTask(any());
    }

    @Test
    @DisplayName("无到期任务时不触发任何处理")
    void 无到期任务() {
        when(timeoutNotifyService.getExpiredTasks()).thenReturn(Collections.emptyList());

        timeoutTaskScheduler.checkExpiredTasks();

        verify(orderDomainService, times(0)).invalidateSmallOrder(anyString());
    }
}
