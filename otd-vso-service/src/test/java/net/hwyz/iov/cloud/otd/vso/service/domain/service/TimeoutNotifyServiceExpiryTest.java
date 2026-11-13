package net.hwyz.iov.cloud.otd.vso.service.domain.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.TimeoutTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 超时任务一次性消费测试（OTD-VSO-DSN-CR-005）
 * <p>
 * 缺陷回归：原实现完成后未从待触发集合移除，任务每分钟被重复执行，
 * 导致同一订单每分钟产生一条 ORDER_INVALIDATE 时间线。
 *
 * @author VSO Team
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("超时任务 - 一次性消费/失败重试")
class TimeoutNotifyServiceExpiryTest {

    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;
    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private TimeoutNotifyService timeoutNotifyService;

    @BeforeEach
    void setUp() throws Exception {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        lenient().when(redisTemplate.keys(anyString())).thenReturn(Collections.emptySet());
    }

    /**
     * 创建并取出一个已到期任务（阈值 0 分钟，创建即到期）
     */
    private TimeoutTask expiredTask(String orderId) {
        String taskId = timeoutNotifyService.createTimeoutTask(orderId, "SMALL_ORDER_PAY_TIMEOUT", "invalid", 0);
        return timeoutNotifyService.getExpiredTasks().stream()
                .filter(task -> taskId.equals(task.getTaskId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("到期任务未返回"));
    }

    @Test
    @DisplayName("完成的任务不再出现在待触发集合（防重复执行）")
    void 完成任务一次性消费() {
        TimeoutTask task = expiredTask("OID1");

        timeoutNotifyService.completeTimeoutTask(task.getTaskId());
        assertEquals("DONE", task.getTaskStatus());

        assertTrue(timeoutNotifyService.getExpiredTasks().isEmpty(), "已完成任务不得被再次触发");
        assertTrue(timeoutNotifyService.getExpiredTasks().isEmpty(), "多次轮询仍不得重复触发");
        verify(redisTemplate).delete("vso:timeout:task:" + task.getTaskId());
    }

    @Test
    @DisplayName("非 PENDING 状态任务不参与触发（DONE/FAILED/CANCELLED）")
    void 非待触发状态不参与() {
        TimeoutTask done = expiredTask("OID1");
        done.complete();

        TimeoutTask cancelled = expiredTask("OID2");
        cancelled.cancel();

        assertTrue(timeoutNotifyService.getExpiredTasks().isEmpty());
    }

    @Test
    @DisplayName("失败重试：未达上限重排，达到上限终止并移除")
    void 失败重试与终止() {
        TimeoutTask task = expiredTask("OID1");

        timeoutNotifyService.failTimeoutTask(task);
        assertEquals("PENDING", task.getTaskStatus(), "未达上限应重排重试");

        timeoutNotifyService.failTimeoutTask(task);
        assertEquals("PENDING", task.getTaskStatus());

        timeoutNotifyService.failTimeoutTask(task);
        assertEquals("FAILED", task.getTaskStatus(), "达到上限应终止");
        assertTrue(timeoutNotifyService.getExpiredTasks().isEmpty(), "终止任务不得再被触发");
    }

    @Test
    @DisplayName("取消后不再触发")
    void 取消后不触发() {
        TimeoutTask task = expiredTask("OID1");

        timeoutNotifyService.cancelByOrderIdAndType("OID1", "SMALL_ORDER_PAY_TIMEOUT");

        assertTrue(timeoutNotifyService.getExpiredTasks().isEmpty());
        assertEquals("CANCELLED", task.getTaskStatus());
    }
}
