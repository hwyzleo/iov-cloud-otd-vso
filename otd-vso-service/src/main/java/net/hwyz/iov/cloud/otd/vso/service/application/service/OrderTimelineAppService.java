package net.hwyz.iov.cloud.otd.vso.service.application.service;

import cn.hutool.core.util.IdUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.OrderTimelineResult;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderTimeline;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderTimelineEventType;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderTimelineRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 订单时间线应用服务
 * <p>
 * 统一时间线写入口，Controller 与 Domain 不得自行拼接时间线。
 *
 * @author VSO Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderTimelineAppService {

    /**
     * 幂等成功事件：重复请求不得重复追加
     */
    private static final Set<OrderTimelineEventType> DEDUPE_SUCCESS_EVENTS = EnumSet.of(
            OrderTimelineEventType.ORDER_CREATED,
            OrderTimelineEventType.PAYMENT_SUCCESS,
            OrderTimelineEventType.EARNEST_TO_DOWN_REQUESTED,
            OrderTimelineEventType.EARNEST_TO_DOWN_COMPLETED,
            OrderTimelineEventType.REFUND_COMPLETED,
            OrderTimelineEventType.ORDER_LOCKED,
            OrderTimelineEventType.TRANSPORT_APPLIED,
            OrderTimelineEventType.PREPARE_TRANSPORT,
            OrderTimelineEventType.TRANSPORTING,
            OrderTimelineEventType.PREPARE_DELIVERY,
            OrderTimelineEventType.ORDER_DELIVERED,
            OrderTimelineEventType.ORDER_COMPLETED
    );

    /**
     * 内部运营事件：仅 MPT 时间线可见，C 端订单进度不得返回（CR-010 INV-022）
     */
    private static final Set<OrderTimelineEventType> INTERNAL_ONLY_EVENTS = EnumSet.of(
            OrderTimelineEventType.DELIVERY_PERSON_ASSIGNED
    );

    private final OrderTimelineRepository orderTimelineRepository;

    /**
     * 记录时间线（成功事件去重，失败事件必须带原因）
     */
    @Transactional(rollbackFor = Exception.class)
    public void record(String orderId, OrderTimelineEventType type, String beforeStatus, String afterStatus,
                       String operatorId, String operatorRole, String operateSource,
                       String relatedDocNo, String failReason, String remark) {
        if (orderId == null || orderId.isEmpty()) {
            log.warn("时间线记录缺少订单ID，跳过：eventType={}", type);
            return;
        }
        if (failReason == null && DEDUPE_SUCCESS_EVENTS.contains(type)
                && isAlreadyRecorded(type, orderId, relatedDocNo)) {
            log.debug("时间线成功事件已存在，跳过重复追加：orderId={}, eventType={}, relatedDocNo={}",
                    orderId, type.getCode(), relatedDocNo);
            return;
        }
        orderTimelineRepository.append(OrderTimeline.of(IdUtil.fastSimpleUUID(), orderId, type,
                beforeStatus, afterStatus, operatorId, operatorRole, operateSource,
                relatedDocNo, failReason, remark));
    }

    /**
     * 成功事件去重：同一订单可多次支付（意向金/补款/尾款），PAYMENT_SUCCESS 按支付单号去重，
     * 其余成功事件保持整单唯一（OTD-VSO-DSN-CR-014：尾款时间线不能被历史支付事件吃掉）。
     */
    private boolean isAlreadyRecorded(OrderTimelineEventType type, String orderId, String relatedDocNo) {
        if (type == OrderTimelineEventType.PAYMENT_SUCCESS
                && relatedDocNo != null && !relatedDocNo.isBlank()) {
            return orderTimelineRepository.existsByOrderIdAndEventTypeAndRelatedDocNo(
                    orderId, type.getCode(), relatedDocNo);
        }
        return orderTimelineRepository.existsByOrderIdAndEventType(orderId, type.getCode());
    }

    /**
     * 查询订单时间线（事件时间正序）
     */
    public List<OrderTimelineResult> timelineOf(String orderId) {
        return orderTimelineRepository.findByOrderId(orderId).stream()
                .map(this::toResult)
                .collect(Collectors.toList());
    }

    /**
     * 查询 C 端可见的订单时间线（过滤内部运营事件）
     * <p>
     * 内部事件（如交付人员分配）含运营操作人信息，默认不对客户端暴露（CR-010 INV-022）。
     */
    public List<OrderTimelineResult> customerTimelineOf(String orderId) {
        return orderTimelineRepository.findByOrderId(orderId).stream()
                .filter(timeline -> !isInternalOnly(timeline.getEventType()))
                .map(this::toResult)
                .collect(Collectors.toList());
    }

    private boolean isInternalOnly(String eventType) {
        if (eventType == null || eventType.isEmpty()) {
            return false;
        }
        return INTERNAL_ONLY_EVENTS.stream()
                .anyMatch(type -> type.getCode().equals(eventType));
    }

    private OrderTimelineResult toResult(OrderTimeline timeline) {
        return OrderTimelineResult.builder()
                .eventType(timeline.getEventType())
                .eventName(timeline.getEventName())
                .eventTime(timeline.getEventTime())
                .beforeStatus(timeline.getBeforeStatus())
                .afterStatus(timeline.getAfterStatus())
                .operatorId(timeline.getOperatorId())
                .operatorRole(timeline.getOperatorRole())
                .operateSource(timeline.getOperateSource())
                .relatedDocNo(timeline.getRelatedDocNo())
                .result(timeline.getResult())
                .failReason(timeline.getFailReason())
                .eventRemark(timeline.getEventRemark())
                .build();
    }
}
